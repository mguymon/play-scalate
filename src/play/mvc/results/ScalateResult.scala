package play.mvc.results

import play.exceptions.UnexpectedException
import play.libs.MimeTypes
import play.mvc.Http.{Request, Response}

private[play] class ScalateResult(content: String, template: String)
  extends Result {

  def apply(request: Request, response: Response) {
    try {
      setContentTypeIfNotSet(response, MimeTypes.getContentType(template, "text/plain"))
      response.out.write(content.getBytes("utf-8"))
    } catch {
      case e: Exception => throw new UnexpectedException(e)
    }
  }

}
