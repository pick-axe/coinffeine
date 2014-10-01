package coinffeine.peer.payment.okpay.ws

import java.net.URI
import scalaxb.{DispatchHttpClientsAsync, Soap11ClientsAsync}

import coinffeine.peer.payment.okpay.generated.BasicHttpBinding_I_OkPayAPIBindings

/** SOAP client of the OKPay service
  *
  * @constructor
  * @param baseAddressOverride  Replace the endpoint specified at the WSDL when present
  */
class OkPayWebService(baseAddressOverride: Option[URI])
  extends BasicHttpBinding_I_OkPayAPIBindings
  with Soap11ClientsAsync
  with DispatchHttpClientsAsync {

  override val baseAddress: URI = baseAddressOverride.getOrElse(super.baseAddress)
  def shutdown(): Unit = {
    OkPayWebService.Log.info("Shutting down OKPay WS client...")
    httpClient.http.shutdown()
  }
}

object OkPayWebService {
  type Service = coinffeine.peer.payment.okpay.generated.I_OkPayAPI
  private val Log = LoggerFactory.getLogger(classOf[OkPayWebService])
}
