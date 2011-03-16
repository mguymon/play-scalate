package play.mvc.scalate

import java.io.File
import play._

import org.fusesource.scalate.DefaultRenderContext
import org.fusesource.scalate.console.ConsoleHelper

class ConsoleWrapper(context: DefaultRenderContext)
  extends ConsoleHelper(context) {

  override def servletContext = null

  override def realPath(uri: String) = {
    new File(Play.applicationPath, "/app/views/" + uri).toString
  }

}
