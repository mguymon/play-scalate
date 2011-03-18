package play.mvc.scalate.exceptions

import java.io.{File, FileInputStream}
import java.util.Arrays

import play.Play
import play.exceptions.SourceAttachment

import org.fusesource.scalate.CompilerException

/**
 * Scalate exception wrapper enhances Play exception handling.
 *
 * @param ex the Scalate compiler exception
 * @author Michael Phan-Ba
 * @since 0.8
 */
class ScalateCompilationException(ex: CompilerException)
  extends ScalateException(ex) with SourceAttachment {

  /**Source lines for the template for this exception */
  private lazy val source = {
    if (ex.errors.size > 0) {
      val file = new File(Play.applicationPath + "/app/views/" + ex.errors(0).file)

      // open file or return null
      val handle = try {
        new FileInputStream(file)
      } catch {
        case _ => null
      }

      if (handle != null) {
        val buffer = new Array[Byte](file.length.toInt)

        // read file into list of lines
        try {
          handle.read(buffer)
          Arrays.asList(new String(buffer).split("\n"): _*)
        } catch {
          case _ => null
        } finally {
          handle.close()
        }
      } else null
    } else null
  }

  /**@see [[SourceAttachment.isSourceAvailable]]*/
  override def isSourceAvailable = source != null

  /**@see [[SourceAttachment.getSourceFile]]*/
  override def getSourceFile = if (isSourceAvailable) ex.errors(0).file else null

  /**@see [[SourceAttachment.getSource]]*/
  override def getSource = source

  /**@see [[SourceAttachment.getLineNumber]]*/
  override def getLineNumber = ex.errors(0).pos.line

  /**@see [[PlayException.getErrorTitle]]*/
  override def getErrorTitle = "Scalate compilation error"

}
