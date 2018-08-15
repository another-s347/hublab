package object Exception {
    final case class HubSessionException(private val message: String = "",
                                         private val cause: Throwable = None.orNull)
        extends Exception(message, cause)

    final case class MessageConvertException(private val message: String = "",
                                             private val cause: Throwable = None.orNull)
        extends Exception(message, cause)

    final case class NotlTaskException(private val message: String = "",
                                       private val cause: Throwable = None.orNull)
        extends Exception(message, cause)
}
