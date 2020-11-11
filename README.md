# OrderBook 
(Note:Project instructions is attached in a word doc called "OrderBookInstructions.doc")

This is a coding project dealing with complicated logic and the algorithm for when trades should occur in stock market.  My main code is the two files **App.java** and **Order.java** under the **src\main\java\com\app folder**.  And the tester file is called BasicAppTest.java under the **src\test\java\com\app folder**. The test cases are under **src\test\resources folder**.  The code passed all test cases and works completely correctly


Instructions (brief):
In this project, you will implement one of the foundational pieces that drive capital markets. Billions of dollars worth of assets are traded on exchanges worldwide daily. Most of those exchanges operate an order book for each instrument that can be traded.
This project is more than just implementing a single function where we test correctness. You will first need to synthesize a possibly foreign subject matter (how order books work), then craft a design based on the description provided, and finally ensure correctness.
We will test for correctness in this project via automated test cases. Inputs are fed into your program via CSV files. Your program will need to generate the correct CSV outputs.
This entire document should be read, sequentially, before you start coding and/or designing.
Definitions
First, we should review some definitions before diving into more specifics.
1.	Orders
An order is an instruction a person gives to either buy or sell something. Orders have several required fields. Critically, an order has the price at and size (number of shares) you are willing to buy or sell.
2.	Bids
"Bids" is the common name used to identify buy-orders. A bid is a single buy-order. Bids are a set of buy-orders.
3.	Asks
"Asks", or "offers", is the common name used to identify sell-orders. An ask is a single sell-order. Asks are a set of sell-orders.
4.	Price
The price specified on a bid or ask. The price specified on a bid is the maximum price a buyer is willing to pay to purchase the stock. The price specified on an ask is the minimum price a seller is willing to pay to sell the stock. Naturally, a buyer will be willing to buy for a price less than their bid, and a seller will be willing to sell at a higher price than their ask.
5.	Trades
The result of a bid-price being ≥ to an ask-price results in a trade. The amount traded is always equal to the ask-price.
6.	Order Book
An order book is a collection of bids and asks, where the bids are sorted such that the top order has the highest price, and the asks are sorted such that the top has the lowest price. The purpose of an order book is to manage the life-cycle of all buy-orders and sell-orders. When an order book processes an incoming order, it has a predefined matching algorithm to figure out what to do with the order.
7.	BBO
The top bid (highest buy price) is called best-bid, and the top ask (lowest sell price) is called best-offer. The BBO is the pair of both the best-bid and best-offer, called the best-bid-offer, or BBO for short. Note that the BBO can contain multiple buy orders (or sell orders), if there are multiple orders at the same price.
Implementation
Your job is to implement an order book that can manage the life cycle of orders. Orders can either be inserted or cancelled. An order that is inserted can update the BBO, and possibly generate trades depending on the price of the order. An order that is cancelled can possibly update the BBO, again depending on the price of the order. Once an order is inserted, the outstanding size of the order can change if it is partially fulfilled. If an order that was previously inserted is completely fulfilled, it is removed from the order book.
You will read insert or cancel requests from a CSV file and apply them to your order book using your matching algorithm, described below.

# Matching Algorithm
An order-book's matching algorithm is responsible for generating trades when an incoming order's price locks or crosses the opposing side of the order book. For example, if the incoming order is to buy 100 shares at $1.00, then the order book will first check if there is an existing sell order with a price less-than-or-equal to $1.00. If there is, then a trade should be generated between the buyer and seller. If not, it will add the buy order to the list of existing bids yet to be fulfilled. When you insert an order, you must ensure that your order book does not become locked or crossed. A locked or crossed order book is where the best-bid price ≥ best-ask price. That should never happen because at least one of the crossing orders should have been fulfilled. A locked or crossed book should never happen if the matching algorithm is working correctly.
Orders can also be partially filled and fulfilled at different prices. For example, consider a bid for 10 shares at $10 with the following three asks already in the order book: (a) 2 shares at $8, (b) 3 shares at $8, and (c) 2 shares at $9. Then, three trades would be generated: (a) 2 shares at $8, (b) 3 shares at $8, and (c) 2 shares at $9. In this case, the bid would have 3 shares remaining at $10. It will then be placed on the bid side of the order book for the remaining 3 shares at $10.

Your matching algorithm should be implemented with price then time priority. This means if you receive two insert orders with the same price, the order with the earliest timestamp takes priority since the prices are the same. If the price and timestamp are the same, then priority should match the ordering in the input file, i.e. whichever order you read first takes priority. This will impact your matching algorithm: if an incoming buy order's price ≥ the existing sell price, and there are multiple sell orders with the same price, then you will match the incoming buy order to the sell order with the earliest timestamp first.
