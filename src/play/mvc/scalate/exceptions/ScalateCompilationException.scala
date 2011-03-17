package play.mvc.scalate.exceptions

import org.fusesource.scalate.CompilerException

/**
 * Scalate exception wrapper enhances Play exception handling.
 *
 * @param ex the Scalate compiler exception
 * @author Michael Phan-Ba
 * @since 0.8
 */
class ScalateCompilationException(ex: CompilerException)
  extends ScalateException(ex) {

  /** @see [[PlayException.getErrorTitle]] */
  override def getErrorTitle = "Scalate compilation error"

}
