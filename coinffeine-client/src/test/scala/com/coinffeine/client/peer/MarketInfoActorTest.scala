package com.coinffeine.client.peer

import akka.testkit.TestProbe

import com.coinffeine.common.Bid
import com.coinffeine.common.Currency.{Euro, UsDollar}
import com.coinffeine.common.Currency.Implicits._
import com.coinffeine.common.exchange.PeerId
import com.coinffeine.common.protocol.gateway.GatewayProbe
import com.coinffeine.common.protocol.messages.brokerage._
import com.coinffeine.common.test.AkkaSpec

class MarketInfoActorTest extends AkkaSpec {

  "A market info actor" should "retrieve a market quote" in new Fixture {
    actor ! MarketInfoActor.RequestQuote(eurMarket)
    gateway.expectForwarding(QuoteRequest(eurMarket), broker)
    gateway.relayMessage(sampleEurQuote, broker)
    expectMsg(sampleEurQuote)
  }

  it should "group concurrent quote requests" in new Fixture {
    val concurrentRequester = TestProbe()

    actor ! MarketInfoActor.RequestQuote(eurMarket)
    gateway.expectForwarding(QuoteRequest(eurMarket), broker)

    concurrentRequester.send(actor, MarketInfoActor.RequestQuote(eurMarket))
    gateway.expectNoMsg()

    gateway.relayMessage(sampleEurQuote, broker)
    expectMsg(sampleEurQuote)
    concurrentRequester.expectMsg(sampleEurQuote)
  }

  it should "retrieve open orders" in new Fixture {
    actor ! MarketInfoActor.RequestOpenOrders(eurMarket)
    gateway.expectForwarding(OpenOrdersRequest(eurMarket), broker)
    gateway.relayMessage(sampleOpenOrders, broker)
    expectMsg(sampleOpenOrders)
  }

  it should "group concurrent open orders requests" in new Fixture {
    val concurrentRequester = TestProbe()

    actor ! MarketInfoActor.RequestOpenOrders(eurMarket)
    gateway.expectForwarding(OpenOrdersRequest(eurMarket), broker)

    concurrentRequester.send(actor, MarketInfoActor.RequestOpenOrders(eurMarket))
    gateway.expectNoMsg()

    gateway.relayMessage(sampleOpenOrders, broker)
    expectMsg(sampleOpenOrders)
    concurrentRequester.expectMsg(sampleOpenOrders)
  }

  it should "handle different markets" in new Fixture {
    actor ! MarketInfoActor.RequestQuote(eurMarket)
    gateway.expectForwarding(QuoteRequest(eurMarket), broker)

    val usdRequester = TestProbe()
    usdRequester.send(actor, MarketInfoActor.RequestQuote(usdMarket))
    gateway.expectForwarding(QuoteRequest(usdMarket), broker)

    gateway.relayMessage(sampleEurQuote, broker)
    expectMsg(sampleEurQuote)

    gateway.relayMessage(sampleUsdQuote, broker)
    usdRequester.expectMsg(sampleUsdQuote)
  }

  trait Fixture {
    val eurMarket = Market(Euro)
    val usdMarket = Market(UsDollar)
    val broker = PeerId("broker")
    val gateway = new GatewayProbe()
    val actor = system.actorOf(MarketInfoActor.props)
    val sampleEurQuote = Quote(spread = 900.EUR -> 905.EUR, lastPrice = 904.EUR)
    val sampleUsdQuote = Quote(spread = 1000.USD -> 1010.USD, lastPrice = 1005.USD)
    val sampleOpenOrders = OpenOrders(OrderSet.empty(eurMarket).addOrder(Bid, 1.5.BTC, 600.EUR))

    actor ! MarketInfoActor.Start(broker, gateway.ref)
    gateway.expectSubscription()
  }
}
