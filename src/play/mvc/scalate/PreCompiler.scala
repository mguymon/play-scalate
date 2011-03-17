package play.mvc.scalate

import java.io._
import play.Play
import org.fusesource.scalate.DefaultRenderContext


object PreCompiler {
  def main(args: Array[String]) {
    //kick off precompiler
    val root = new File(System.getProperty("application.path"));
    Play.init(root, System.getProperty("play.id", ""));
    play.Logger.info("Precompiling scalate templates...")
    Play.start()
    val precompiler = new PrecompilerProvider()
    precompiler.precompileTemplates
  }


}

class PrecompilerProvider extends Provider {

  val compile: String => Unit = (filePath: String) => {
    val playPath = filePath.replace((new File(Play.applicationPath + "/app/views")).toString, "")

    play.Logger.info("compiling: " + playPath + " to:" + engine.bytecodeDirectory + " ...")

    val buffer = new StringWriter()
    // TODO: set uri
    var context = new DefaultRenderContext(null, engine, new PrintWriter(buffer))

    // populate playcontext
    context.attributes("playcontext") = PlayContext
    context.attributes("javax.servlet.error.exception") = new Exception
    context.attributes("javax.servlet.error.message") = ""

    //compile template
    try {
      engine.load(playPath)
    } catch {
      case ex: ClassCastException =>
    }
  }

  def precompileTemplates = walk(new File(Play.applicationPath, "/app/views"))(compile)

  def walk(file: File)(func: String => Unit): Boolean = {
    val fileName = file.getName
    if (file.isFile && (
      fileName.endsWith(".ssp") ||
        fileName.endsWith(".scaml") ||
        fileName.endsWith(".jade") ||
        fileName.endsWith(".mustache")) &&
      !fileName.contains("default.ssp") &&
      !fileName.contains("default.scaml") &&
      !fileName.contains("default.jade") &&
      !fileName.contains("default.mustache"))
      func(file.getPath)

    if (file.isDirectory)
      for (i <- 0 until file.listFiles.length) walk(file.listFiles()(i))(func)

    true
  }

}
