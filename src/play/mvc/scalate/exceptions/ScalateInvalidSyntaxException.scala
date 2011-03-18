package play.mvc.scalate.exceptions

import java.util.Arrays
import play.exceptions.SourceAttachment
import org.fusesource.scalate.InvalidSyntaxException

/**
 * Scalate exception wrapper enhances Play exception handling.
 *
 * @param ex the Scalate invalid syntax exception
 * @author Michael Phan-Ba
 * @since 0.8
 */
class ScalateInvalidSyntaxException(ex: InvalidSyntaxException)
  extends ScalateException(ex) with SourceAttachment {

  /** @see [[SourceAttachment.isSourceAvailable]] */
  override def isSourceAvailable = ex.source != null

  /** @see [[SourceAttachment.getSourceFile]] */
  override def getSourceFile = ex.template

  /** @see [[SourceAttachment.getSource]] */
  override def getSource =
    if (ex.source != null) Arrays.asList(ex.source.text.split("\n"): _*)
    else null

  /** @see [[SourceAttachment.getLineNumber]] */
  override def getLineNumber = ex.pos.line

  /** @see [[SourceAttachment.getErrorTitle]] */
  override def getErrorTitle = "Scalate invalid syntax error"

}
