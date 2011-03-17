package play.mvc.scalate.exceptions

import play.exceptions.PlayException
import org.fusesource.scalate.TemplateException

/**
 * Scalate exception wrapper enhances Play exception handling.
 *
 * @param ex the Scalate template exception
 * @author Michael Phan-Ba
 * @since 0.8
 */
class ScalateException(ex: TemplateException)
  extends PlayException(ex.getMessage, ex) {

  /** @see [[PlayException.getErrorTitle]] */
  def getErrorTitle = "Scalate error"

  /** @see [[PlayException.getErrorDescription]] */
  def getErrorDescription = "Caught Scalate exception: " + ex

}
