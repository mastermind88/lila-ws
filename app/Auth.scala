package lichess.ws

import javax.inject._

import play.api.mvc.RequestHeader
import reactivemongo.bson._
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
final class Auth @Inject() (mongo: Mongo)(implicit executionContext: ExecutionContext) {

  private val sidRegex = """.*sessionId=(\w+).*""".r

  def apply(req: RequestHeader): Future[Option[User]] = {
    req.cookies get "lila2" match {
      case Some(cookie) =>
        val sid = sidRegex.replaceAllIn(cookie.value, "$1")
        mongo.security {
          _.find(
            BSONDocument("_id" -> sid, "up" -> true),
            Some(BSONDocument("_id" -> false, "user" -> true))
          ).one[BSONDocument]
        } map {
          _.flatMap {
            _.getAs[String]("user") map User.apply
          }
        }
      case None => Future.successful(None)
    }
  }
}
