package com.app;

public class Order {
    //timestamp,action,order_id,side,price,size
    private int timestamp;
    private String action;
    private int order_id;
    private String side; //buy or sell
    private int price;
    private int size;

    public Order(int timestamp, String action, int order_id, String side, int price, int size)
    {
        this.timestamp = timestamp;
        this.action = action;
        this.order_id = order_id;
        this.side = side;
        this.price = price;
        this.size = size;
    }

    //overrides the equals method, checking if this order equals order param based on id
    public boolean equals(Order order)
    {
        if (this.getOrder_id() == order.getOrder_id())
            return true;
        else
            return false;
    }

    public int getTimestamp()
    {
        return timestamp;
    }

    @Override
    public String toString()
    {
        return "Order{" +
                "timestamp=" + timestamp +
                ", action='" + action + '\'' +
                ", order_id=" + order_id +
                ", side='" + side + '\'' +
                ", price=" + price +
                ", size=" + size +
                '}';
    }

    public void setTimestamp(int timestamp)
    {
        this.timestamp = timestamp;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public int getOrder_id()
    {
        return order_id;
    }

    public void setOrder_id(int order_id)
    {
        this.order_id = order_id;
    }

    public String getSide()
    {
        return side;
    }

    public void setSide(String side)
    {
        this.side = side;
    }

    public int getPrice()
    {
        return price;
    }

    public void setPrice(int price)
    {
        this.price = price;
    }

    public int getSize()
    {
        return size;
    }

    public void setSize(int size)
    {
        this.size = size;
    }
}
