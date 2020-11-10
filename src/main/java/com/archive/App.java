package com.archive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class App {
    //todo: mark these fields as private
    StringBuilder bboPath;
    StringBuilder tradePath;
    Map<Integer, Order> map = new LinkedHashMap<>(); //order id, Order object
    Map<Integer, List<Order>> priceMapBuy = new HashMap<>(); //price, list of Order objects containing that price
    Map<Integer, List<Order>> priceMapSell = new HashMap<>(); //price, list of Order objects containing that price
    FileWriter inputFile;
    FileWriter outputBbosFile;
    FileWriter outputTradesFile;

    PriorityQueue<Order> maxHeapBids = new PriorityQueue<>(new Comparator<Order>()
    {
        //first order by highest price
        //if prices are equal, then order by lower timestamp
        //if timestamps are equal, then order by lower orderID
        @Override
        public int compare(Order e1, Order e2)
        {
            if (e1.getPrice() != e2.getPrice())
                return e2.getPrice()-e1.getPrice(); //highest price on top
            else //prices equal
            {
                if (e1.getTimestamp() != e2.getTimestamp())
                    return e1.getTimestamp()-e2.getTimestamp(); //earlier timestamp has priority
                else //timestamps equal
                    return e1.getOrder_id()-e2.getOrder_id();
            }
        }
    });
    PriorityQueue<Order> minHeapAsks = new PriorityQueue<>(new Comparator<Order>()
    {
        //first order by lowest price
        //if prices are equal, order by lower timestamp
        //if timestamps are equal, order by lower orderID
        @Override
        public int compare(Order e1, Order e2)
        {
            if (e1.getPrice() != e2.getPrice())
                return e1.getPrice()- e2.getPrice(); //TODO
            else //prices are equal
            {
                if (e1.getTimestamp() != e2.getTimestamp())
                    return e1.getTimestamp() - e2.getTimestamp();
                else //time stamps are equal, so order by lower orderID
                    return e1.getOrder_id()-e2.getOrder_id();
            }
        }
    });
    // Your application must define a class named `App` with a method named `run`. This
    // method will be imported by the test runner. The `run` method receives the following
    // arguments:
    // * `inputPath`: Path to the input CSV file
    // * `bboPath`: Path to the file you construct that contains BBO CSV output
    // * `tradePath`: Path to the file you construct that ontains trades CSV output
    public void run(String inputPath, StringBuilder bboPath, StringBuilder tradePath) throws FileNotFoundException {
        this.bboPath = bboPath;
        this.tradePath = tradePath;
        File inputFile = new File(inputPath); //C:\Users\sethy\OneDrive for Business\takeHomeProjects\ClearStreet\src\main\resources

        bboPath.append(inputPath.substring(0, inputPath.length()-9)).append("output_bbos.csv");
        tradePath.append(inputPath.substring(0, inputPath.length()-9)).append("output_trades.csv");
        try
        {
            outputBbosFile = new FileWriter(bboPath.toString());
            outputBbosFile.write("bid_price,bid_size,ask_price,ask_size");
            outputBbosFile.write("\n");

        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            outputTradesFile = new FileWriter(tradePath.toString());
            outputTradesFile.write("trade_price,trade_size,buy_order_id,sell_order_id");
            outputTradesFile.write("\n");

        }
        catch(IOException e)
        {
            e.printStackTrace();
        }


        Scanner scnr = new Scanner(inputFile);

        //Reading each line of file using Scanner class
        int lineNumber = 1;

        //header
        String header = scnr.nextLine();

        while(scnr.hasNextLine()){
            String line = scnr.nextLine(); //read in a line representing one order
            String[] fields = line.split("[,]", -1);
            //timestamp,action,order_id,side,price,size
            Order order = null;
            if (fields[1].equals("insert"))
            {
                order = new Order(Integer.parseInt(fields[0]), fields[1], Integer.parseInt(fields[2]), fields[3],
                        Integer.parseInt(fields[4]), Integer.parseInt(fields[5]));
                map.put(order.getOrder_id(), order); //insert into the id map

                if (order.getSide().equals("buy"))
                {
                    priceMapBuy.putIfAbsent(order.getPrice(), new ArrayList<>());
                    priceMapBuy.get(order.getPrice()).add(order);
                    buyOrder(order, bboPath, tradePath);
                }
                else if (order.getSide().equals("sell"))
                {
                    priceMapSell.putIfAbsent(order.getPrice(), new ArrayList<>());
                    priceMapSell.get(order.getPrice()).add(order);
                    sellOrder(order, bboPath, tradePath);
                }
            }
            else //cancel order (ignore side, price,size)
            {
                int id = Integer.parseInt(fields[2]); //the unique id for the order
//                Order order = new Order(Integer.parseInt(fields[0]), fields[1], Integer.parseInt(fields[2]));
                cancelOrder(id, bboPath, tradePath);
            }
            lineNumber++;
        }

        try {
            outputBbosFile.flush();
            outputBbosFile.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            outputTradesFile.flush();
            outputTradesFile.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void buyOrder(Order newOrder, StringBuilder bboPath, StringBuilder tradePath)
    {
        maxHeapBids.add(newOrder);
        Order maxBid = maxHeapBids.peek(); //now we have the max bid
        boolean isBidRemoved = false;


        //make sure a sell exists in OrderBook and make sure the bid price >= the min ask price
        while (!minHeapAsks.isEmpty() && maxBid.getPrice() >= minHeapAsks.peek().getPrice())
        {
            Order minAsk = minHeapAsks.peek();
            int trade_price = minAsk.getPrice(); //by def, trade price is always equal to the ask price
            int trade_size = Math.min(minAsk.getSize(), maxBid.getSize());
            int buy_order_id = maxBid.getOrder_id();
            int sell_order_id = minAsk.getOrder_id();

            //if there are no orders on a particular side, both the price and size for that
            //particular side should be set to 0
            int bid_price = 0;
            int bid_size = 0;
            int ask_price = 0;
            int ask_size = 0;

            //if the buy size equals the lowest ask size
            if (maxBid.getSize() == minAsk.getSize())
            {
                isBidRemoved = true; //buy will be removed from order book

                minHeapAsks.remove(); //O(1)
                maxHeapBids.remove();
                map.remove(minAsk.getOrder_id());
                map.remove(maxBid.getOrder_id());
                //remove the trade pair from priceMap(max bid and min ask)
                removeFromPriceMap(maxBid, priceMapBuy);

                removeFromPriceMap(minAsk, priceMapSell);

//                tradePath.write(trade_price+","+trade_size+","+buy_order_id+","+sell_order_id);
                writeToTradesFile(tradePath, trade_price, trade_size, buy_order_id, sell_order_id);
                //get best offer (lowest ask price after removing minAsk)
                if (!minHeapAsks.isEmpty())//there are sell orders still in the minHeap
                {
                    //find new minAsk price
                    ask_price = minHeapAsks.peek().getPrice(); //best ask price
                    ask_size = getBidOrAsk_size(ask_price, ask_size, priceMapSell);
                }
                //get next best bid (highest buy price after removing maxBid)
                if (!maxHeapBids.isEmpty())
                {
                    bid_price = maxHeapBids.peek().getPrice();
                    //find all occurrences of that price  in maxBid to get the net size on the best buy price
                    bid_size = getBidOrAsk_size(bid_price, bid_size, priceMapBuy);
                }
                //need to write output to BBO output file
                writeToBBOFile(bboPath, bid_price, bid_size, ask_price, ask_size);
                break; //done with this maxBid
            }

            //the ask has the lower size
            if (trade_size == minAsk.getSize())
            {
                isBidRemoved = false; //case when buy has 1000 shares

                //remove the minAsk from the minHeap since you exhausted that sell
                minHeapAsks.poll();
                //remove minAsk from map and price map sell
                map.remove(minAsk.getOrder_id());
                removeFromPriceMap(minAsk, priceMapSell);
                //write to output_trades file
                writeToTradesFile(tradePath, trade_price, trade_size, buy_order_id, sell_order_id);

                //decrement the size of the buy maxBid by trade_size
                maxBid.setSize(maxBid.getSize()-trade_size);
            }
            //the buy has lower size than the min ask
            else
            {
                isBidRemoved = true; //buy will be removed from order book

                //remove maxBid from the maxHeap, map, and price map buy
                maxHeapBids.poll();
                map.remove(maxBid.getOrder_id());
                removeFromPriceMap(maxBid, priceMapBuy);
                //decrement the ask size by the buy size
                minAsk.setSize(minAsk.getSize()-trade_size);

                //write to output_trades file
                writeToTradesFile(tradePath, trade_price, trade_size, buy_order_id, sell_order_id);

                //get new bbo and write it to output_bbos file
                //get best offer (lowest ask price after removing minAsk)
                if (!minHeapAsks.isEmpty())//there are sell orders still in the minHeap
                {
                    //find new minAsk price
                    ask_price = minHeapAsks.peek().getPrice(); //best ask price
                    ask_size = getBidOrAsk_size(ask_price, ask_size, priceMapSell);
                }
                //get next best bid (highest buy price after removing maxBid)
                if (!maxHeapBids.isEmpty())
                {
                    bid_price = maxHeapBids.peek().getPrice();
                    //find all occurrences of that price  in maxBid to get the net size on the best buy price
                    bid_size = getBidOrAsk_size(bid_price, bid_size, priceMapBuy);
                }
                writeToBBOFile(bboPath, bid_price, bid_size, ask_price, ask_size);

                break; //break out of while loop because we are done since we removed the buy order
            }
        }


        writeBBONoTrade(isBidRemoved, bboPath);
    }
    private void writeToBBOFile(StringBuilder bboPath, int bid_price, int bid_size, int ask_price, int ask_size)
    {
        try
        {

            outputBbosFile.write(String.valueOf(bid_price) +","+String.valueOf(bid_size)+","+
                    String.valueOf(ask_price)+","+String.valueOf(ask_size));
            outputBbosFile.write("\n");
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

    }
    private void writeToTradesFile(StringBuilder tradePath, int trade_price, int trade_size, int buy_order_id, int sell_order_id)
    {
        try
        {
            outputTradesFile.write(String.valueOf(trade_price) +","+String.valueOf(trade_size)+","+
                                    String.valueOf(buy_order_id)+","+String.valueOf(sell_order_id));
            outputTradesFile.write("\n");
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    //helper method provided by intellij
    //note: priceMap is either priceMapBuy or priceMapSell depending on what was passed in
    private int getBidOrAsk_size(int ask_price, int ask_size, Map<Integer, List<Order>> priceMap)
    {
        //find all occurrences of that price to get the net size on the best buy/ask price
        List<Order> bestPrices = priceMap.get(ask_price);
        for (Order o : bestPrices)
        {
            ask_size += o.getSize(); //sum up all of the sizes
        }
        return ask_size; //either buy or ask size
    }

    //only write to the bbo output file if the buy was not removed from order book
    //since in the 2 cases when buy was removed in buyOrder(), we already wrote to bbo output file
    private void writeBBONoTrade(boolean isBidRemoved, StringBuilder bboPath)
    {
        Order maxBid;
        if (!isBidRemoved)
        {
            int bid_price = 0;
            int bid_size = 0;
            int ask_price = 0;
            int ask_size = 0;

            if (!maxHeapBids.isEmpty())
            {
                maxBid = maxHeapBids.peek();
                bid_price = maxBid.getPrice();
                //find all occurrences of that price  in maxBid to get the net size on the best buy price
                bid_size = getBidOrAsk_size(bid_price, bid_size, priceMapBuy);
            }


            if (!minHeapAsks.isEmpty())
            {
                ask_price = minHeapAsks.peek().getPrice(); //best ask price
                //find all occurrences of that price to get the net size on the best ask price
                ask_size = getBidOrAsk_size(ask_price, ask_size, priceMapSell);
            }

            writeToBBOFile(bboPath, bid_price, bid_size, ask_price, ask_size);
        }
    }


    //helper method from intellij
    //note: priceMap can be priceMapBuy or priceMapSell
    private void removeFromPriceMap(Order maxBid, Map<Integer, List<Order>> priceMap)
    {
        List<Order> temp = priceMap.get(maxBid.getPrice());
        for (Order o : temp)
        {
            if (o.equals(maxBid)) //based on id
            {
                temp.remove(o);
                break;
            }
        }
    }

    //TODO
    private void sellOrder(Order ord, StringBuilder bboPath, StringBuilder tradePath)
    {
        /*
        already done:
                priceMap.putIfAbsent(order.getPrice(), new ArrayList<>());
                priceMap.get(order.getPrice()).add(order);
                map.put(order.getOrder_id(), order); //insert into the id map
         */
        minHeapAsks.add(ord);
        Order minAsk = minHeapAsks.peek(); //now we have the min ask
        boolean isAskRemoved = false;

        //make sure a buy exists in OrderBook and make sure the bid price >= the min ask price
        while (!maxHeapBids.isEmpty() && maxHeapBids.peek().getPrice() >= minAsk.getPrice())
        {
            Order maxBid = maxHeapBids.peek();
            int trade_price = minAsk.getPrice(); //by def, trade price is always equal to the ask price
            int trade_size = Math.min(minAsk.getSize(), maxBid.getSize());
            int buy_order_id = maxBid.getOrder_id();
            int sell_order_id = minAsk.getOrder_id();

            //if there are no orders on a particular side, both the price and size for that
            //particular side should be set to 0
            int bid_price = 0;
            int bid_size = 0;
            int ask_price = 0;
            int ask_size = 0;

            if (maxBid.getSize() == minAsk.getSize())
            {
                isAskRemoved = true;
                minHeapAsks.remove();
                maxHeapBids.remove();
                map.remove(maxBid.getOrder_id());
                map.remove(minAsk.getOrder_id());
                //remove the trade pair from priceMap(max bid and min ask)
                removeFromPriceMap(maxBid, priceMapBuy);

                removeFromPriceMap(minAsk, priceMapSell);

                writeToTradesFile(tradePath, trade_price, trade_size, buy_order_id, sell_order_id);

                //get best offer (lowest ask price after removing minAsk)
                if (!minHeapAsks.isEmpty())//there are sell orders still in the minHeap
                {
                    minAsk = minHeapAsks.peek(); //find new minAsk
                    ask_price = minAsk.getPrice(); //best ask price
                    //find all occurrences of that price  in order to get the net size on the best ask price
                    ask_size = getBidOrAsk_size(ask_price, ask_size, priceMapSell);
                }
                //get next best bid (highest buy price after removing maxBid)
                if (!maxHeapBids.isEmpty())
                {
                    maxBid = maxHeapBids.peek();
                    bid_price = maxBid.getPrice();
                    //find all occurrences of that price  in order to get the net size on the best buy price
                    bid_size = getBidOrAsk_size(bid_price, bid_size, priceMapBuy);
                }
                //need to write output to BBO output file
                writeToBBOFile(bboPath, bid_price, bid_size, ask_price, ask_size);

                break; //done with this order
            }


            //the buy has the lower size
            if (trade_size == maxBid.getSize())
            {
                isAskRemoved = false; //case when buy has 1000 shares

                //remove the maxBid from the maxHeap since you exhausted that buy
                maxHeapBids.poll();
                //remove minAsk from map and price map sell
                map.remove(maxBid.getOrder_id());
                removeFromPriceMap(maxBid, priceMapBuy);
                //write to output_trades file
                writeToTradesFile(tradePath, trade_price, trade_size, buy_order_id, sell_order_id);

                //decrement the size of the sell minAsk by trade_size
                minAsk.setSize(minAsk.getSize()-trade_size);
            }
            //the ask has the lower size than the maxBid
            else
            {
                isAskRemoved = true; //buy will be removed from order book

                //remove minAsk from the minHeap, map, and price map sell
                minHeapAsks.poll();
                map.remove(minAsk.getOrder_id());
                removeFromPriceMap(minAsk, priceMapSell);
                //decrement the buy size by the ask size
                maxBid.setSize(maxBid.getSize()-trade_size);

                //write to output_trades file
                writeToTradesFile(tradePath, trade_price, trade_size, buy_order_id, sell_order_id);

                //get new bbo and write it to output_bbos file
                //get best offer (lowest ask price after removing minAsk)
                if (!minHeapAsks.isEmpty())//there are sell orders still in the minHeap
                {
                    //find new minAsk price
                    ask_price = minHeapAsks.peek().getPrice(); //best ask price
                    ask_size = getBidOrAsk_size(ask_price, ask_size, priceMapSell);
                }
                //get next best bid (highest buy price after removing maxBid)
                if (!maxHeapBids.isEmpty())
                {
                    bid_price = maxHeapBids.peek().getPrice();
                    //find all occurrences of that price  in maxBid to get the net size on the best buy price
                    bid_size = getBidOrAsk_size(bid_price, bid_size, priceMapBuy);
                }
                writeToBBOFile(bboPath, bid_price, bid_size, ask_price, ask_size);

                break; //break out of while loop because we are done since we removed the buy order
            }

        }

        writeBBONoTrade(isAskRemoved, bboPath);

    }

    //TODO
    //assume there is an existing insert we order id: orderID, and we need to cancel this order
    //by removing it from the maps and heaps, and then figuring out the new bbo
    private void cancelOrder(int orderID, StringBuilder bboPath, StringBuilder tradePath)
    {
        Order minAsk;
        Order maxBid;


        //if there are no orders on a particular side, both the price and size for that
        //particular side should be set to 0
        int bid_price = 0;
        int bid_size = 0;
        int ask_price = 0;
        int ask_size = 0;

        //*****only cancel an order that already exists and update bbo if necessary
        if (map.containsKey(orderID))
        {
            //get the order with map.get
            Order order = map.get(orderID);
            //see if the order is a buy or sell using order.getSide()
            if (order.getSide().equals("buy"))        //if the order is a buy, then remove from the maxHeap of buys
            {
                maxHeapBids.remove(order);
                removeFromPriceMap(order, priceMapBuy);

            }
            //else if the order is a sell, remove from the minHeap of sells
            else
            {
                minHeapAsks.remove(order);
                removeFromPriceMap(order, priceMapSell);
            }

            //remove entry from the map using map.remove(id)
            map.remove(orderID);

            //figure out new best bid and best ask
            //get best offer (lowest ask price after removing minAsk)
            if (!minHeapAsks.isEmpty())//there are sell orders still in the minHeap
            {
                minAsk = minHeapAsks.peek(); //find new minAsk
                ask_price = minAsk.getPrice(); //best ask price
                //find all occurrences of that price  in order to get the net size on the best ask price
                ask_size = getBidOrAsk_size(ask_price, ask_size, priceMapSell);
            }
            //get next best bid (highest buy price after removing maxBid)
            if (!maxHeapBids.isEmpty())
            {
                maxBid = maxHeapBids.peek();
                bid_price = maxBid.getPrice();
                //find all occurrences of that price  in order to get the net size on the best buy price
                bid_size = getBidOrAsk_size(bid_price, bid_size, priceMapBuy);
            }
            writeToBBOFile(bboPath, bid_price, bid_size, ask_price, ask_size);
        }
        else
        {
            //get best offer (lowest ask price after removing minAsk)
            if (!minHeapAsks.isEmpty())//there are sell orders still in the minHeap
            {
                minAsk = minHeapAsks.peek(); //find new minAsk
                ask_price = minAsk.getPrice(); //best ask price
                //find all occurrences of that price  in order to get the net size on the best ask price
                ask_size = getBidOrAsk_size(ask_price, ask_size, priceMapSell);
            }
            //get next best bid (highest buy price after removing maxBid)
            if (!maxHeapBids.isEmpty())
            {
                maxBid = maxHeapBids.peek();
                bid_price = maxBid.getPrice();
                //find all occurrences of that price  in order to get the net size on the best buy price
                bid_size = getBidOrAsk_size(bid_price, bid_size, priceMapBuy);
            }
            writeToBBOFile(bboPath, bid_price, bid_size, ask_price, ask_size);
        }
    }
}