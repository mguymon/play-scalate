package play.mvc

import play.mvc.scalate.Provider

abstract class ScalateController extends ScalaController with Provider {

  implicit def scalateStringAsTemplateName(name: String) = new {
    def asTemplate(args: (Symbol, Any)*) = renderWithScalate(name, args)
    def asTemplate = renderWithScalate(name)
  }

  private lazy val not_reached: play.mvc.results.Template = null

  override def Template = {
    renderWithScalate()

    not_reached // This line must not been executed.
  }

  override def Template(_args: (Symbol, Any)*) = {
    renderWithScalate(args = _args)

    not_reached // This line must not been executed.    
  }
  
  override def Template(name: String, args: (Symbol, Any)*) = {
	renderWithScalate(name, args)

    not_reached // This line must not been executed. 
  }
}
