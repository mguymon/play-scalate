package play.mvc.scalate

import java.io.{ File, PrintWriter, StringWriter }

import scala.collection.JavaConversions._

import play.{ Logger, Play }
import play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer
import play.data.validation.Validation
import play.exceptions._
import play.mvc.{ Scope, Http }
import play.mvc.results.ScalateResult
import play.mvc.scalate.exceptions._

import org.fusesource.scalate._
import org.fusesource.scalate.layout.DefaultLayoutStrategy
import org.fusesource.scalate.util.{ FileResourceLoader, SourceCodeHelper }

trait Provider {

  def renderWithScalate(templateName: String = null, args: Seq[(Symbol, Any)] = Seq[(Symbol, Any)]()) {
    //determine template
    val _templateName: String =
      if (templateName != null) {
        templateName
      } else if (args.length > 0 && args(0)._1 == 'template &&
        LocalVariablesNamesTracer.getAllLocalVariableNames(args(0)_2).isEmpty) {
        discardLeadingAt(args(0)._2.toString)
      } else {
        determineURI()
      }

    if (shouldRenderWithScalate(_templateName)) {
      renderScalateTemplate(_templateName, args)
    } else {
      throw new RuntimeException("could not find scalate template")
    }
  }

  val bytecodeDirectory: Option[File] = None

  // Create and configure the Scalate template engine
  private def initEngine(usePlayClassloader: Boolean = true, customImports: String = "import controllers._;import models._;import play.utils._"): TemplateEngine = {
    val engine = new TemplateEngine {
      override def bytecodeDirectory =
        Provider.this.bytecodeDirectory.getOrElse(super.bytecodeDirectory)
    }

    val playcontext = SourceCodeHelper.name(PlayContext.getClass).dropRight(1) + ".type"
    engine.bindings = List(
      Binding("context", SourceCodeHelper.name(classOf[DefaultRenderContext]), true),
      Binding("playcontext", playcontext, true))
    engine.importStatements ++= List(customImports)
    if (Play.mode == Play.Mode.PROD && Play.configuration.getProperty("scalate.allowReloadInProduction") == null) engine.allowReload = false

    engine.workingDirectory = new File(Play.applicationPath, "/tmp")
    engine.resourceLoader = new FileResourceLoader(Some(new File(Play.applicationPath + "/app/views")))

    val classpathRoot = if (new File(Play.applicationPath, "/precompiled/java").exists && Play.mode == Play.Mode.PROD)
      new File(Play.applicationPath, "/precompiled/java").toString
    else
      new File(Play.applicationPath, "/tmp/classes").toString

    engine.classpath = classpathRoot
    engine.combinedClassPath = true

    val defaultLayout = renderMode.map("/default." + _)

    engine.layoutStrategy = new DefaultLayoutStrategy(engine, defaultLayout: _*)

    if (usePlayClassloader) engine.classLoader = Play.classloader

    engine
  }

  private val defaultRenderMode = Array("ssp", "scaml", "jade", "mustache")

  def renderMode = {
    val renderMode = Play.configuration.getProperty("scalate.rendermode")
    if (renderMode == null) defaultRenderMode
    else renderMode.split(",").map(_.trim).distinct
  }

  val engine = initEngine()

  def defaultTemplate = Http.Request.current().action

  def requestFormat = Http.Request.current().format

  def controller = Http.Request.current().controller

  def validationErrors = Validation.errors

  private def renderScalateTemplate(templateName: String, args: Seq[(Symbol, Any)]) {
    //loading template
    val buffer = new StringWriter()
    // TODO: set uri
    var context = new DefaultRenderContext(null, engine, new PrintWriter(buffer))
    val renderArgs = Scope.RenderArgs.current()

    // try to fill context
    for (o <- args)
        context.attributes(o._1.name) = o._2

    context.attributes("playcontext") = PlayContext

    // now add renderArgs as well
    if (renderArgs != null && renderArgs.data != null) {
      for (pair <- renderArgs.data) context.attributes(pair._1) = pair._2
    }

    try {
      context.attributes("errors") = validationErrors
    } catch {
      case ex: Exception => throw new UnexpectedException(ex)
    }

    val templatePath = findTemplatePath(templateName).getOrElse {
      throw new TemplateNotFoundException(templateName)
    }

    def handleSpecialError(ex: Throwable) {
      context.attributes("javax.servlet.error.exception") = ex
      context.attributes("javax.servlet.error.message") = ex.getMessage

      val errorTemplate = findTemplatePath("errors/500.html").getOrElse {
        // pass through to default error template
        throw ex
      }

      try {
        engine.layout(engine.load(errorTemplate), context)
      } catch {
        case ex =>
          // prevent infinite loops when the error template has errors
          Logger.error(ex, "Exception while rendering error page")
          throw new ScalateResult("500 Internal Server Error", templateName)
      }

      throw new ScalateResult(buffer.toString, templateName)
    }

    try {
      engine.layout(templatePath, context)
    } catch {
      case ex: TemplateNotFoundException =>
        if (!ex.isSourceAvailable) {
          val element = PlayException.getInterestingStrackTraceElement(ex)
          if (element != null) {
            throw new TemplateNotFoundException(templateName,
              Play.classes.getApplicationClass(element.getClassName()),
              element.getLineNumber());
          }
        }
        throw ex
      case ex: InvalidSyntaxException =>
        handleSpecialError(new ScalateInvalidSyntaxException(ex))
      case ex: CompilerException =>
        handleSpecialError(new ScalateCompilationException(ex))
      case ex => handleSpecialError(ex)
    }

    throw new ScalateResult(buffer.toString, templateName)
  }

  private def findTemplatePath(templateName: String): Option[String] = {
    for (mode <- renderMode) {
      val baseName = templateName.replaceAll(".html", "." + mode)
      val viewPath = Play.applicationPath + "/app/views"
      val file = new File(viewPath, "/" + baseName)
      if (file.isFile)
        return Some(file.toString.replace(new File(viewPath).toString, ""))
    }
    None
  }

  // determine if we need to render with scalate
  private def shouldRenderWithScalate(template: String): Boolean = {
    val ignore = Play.configuration.getProperty("scalate.ignore")
    ignore == null ||
      ignore.split(",").map(_.trim).distinct.filter(template.startsWith(_)).size != 0
  }

  private def discardLeadingAt(templateName: String): String = {
    if (templateName.startsWith("@")) {
      determineURI {
        if (!templateName.contains("."))
          controller + "." + templateName.substring(1)
        else
          templateName.substring(1)
      }
    } else templateName
  }

  private def determineURI(template: String = defaultTemplate): String = {
    template.replace(".", "/") + "." +
      (if (requestFormat == null) "html" else requestFormat)
  }

}
