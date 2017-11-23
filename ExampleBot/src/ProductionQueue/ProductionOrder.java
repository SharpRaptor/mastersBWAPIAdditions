package ProductionQueue;

/**
 * Abstract base class for the different types of Production Order
 */
public abstract class ProductionOrder implements Comparable<ProductionOrder>{

    public ProductionOrder() {}

    /**
     * Used to Differentiate Order Status
     * commissioned - on the queue but not yet acted upon. Not enough resources or not a priority
     * ordered - Enough Resources have been gathered, there is a free to produce unit and the order has been passed back to the users bot to begin production.
     * started - work on the has begun.
     * aborted - when a job has been cancelled but has not been cleared off the queue yet.
     */
    public enum OrderStatus{
        commissioned, ordered, started, finished, aborted
    }

    protected int priority;
    protected int orderTime;
    protected OrderStatus status;


    //Used for ordering lists of production orders. The list will have orders with high priority above lower priorities, then on items with the same priority, the older items will be higher up the list.
    @Override
    public int compareTo(ProductionOrder commandIn)
    {
        int priorityResult = commandIn.getPriority() - this.priority;
        // + This has Higher
        // 0 Equal Priority
        // - This has lower
        if (priorityResult != 0)
        {
            return (priorityResult / Math.abs(priorityResult));
        }

        int timeResult = this.orderTime - commandIn.getOrderTime();
        // + This was older
        // 0 Equal Priority
        // - This is more recent
        if (timeResult != 0)
        {
            return (timeResult / Math.abs(timeResult));
        }

        return 0;
    }

    /**
     * Gets the priority of the production order
     * @return Int Priority : Higher the number the higher the priority.
     */
    public int getPriority() {return priority;}

    /**
     * Gets the time that the order was made
     * @return Int orderTime : The number of frames that had passed when the order was made.
     */
    public int getOrderTime() {return orderTime;}

    /**
     * Gets the status of the order
     * @return OrderStatus enum CurrentStatus : The current status of the order
     */
    public OrderStatus getStatus() {return status;}

    /**
     * Sets the status of the current order
     * @param statusIn The status to set the order to
     */
    public void setStatus(OrderStatus statusIn) {this.status = statusIn;}

    @Override
    public abstract String toString();

    /**
     * Run a check to see if the current player can afford to purchase what the order s going to create
     * @return Boolean : true means the player can afford it, false means they can't
     */
    public abstract boolean canAfford();

    /**
     *Run a check to see whether there is a building that can produce this item and if it is idle
     * @return True if there is an idle building of the type needed. False if there isn't.
     */
    public abstract boolean isToProduceFree();

    /**
     * Run a check to see if order has been started. Only to be run if the order is 'ordered'. Does nothing for UnitBuildingOrders.
     * If it is started then the status in the order is set to started.
     * @return Boolean. True if the order has started. False if it hasn't
     */
    public abstract boolean checkHasStarted();

    /**
     * Run a check to se if the order has finished. Only to be run if the order is 'started'.
     * It it is finished then the status in the order is set to finished.
     * @return Boolean. True if the order is finished. False if it isn't.
     */
    public abstract boolean checkHasFinished();
}
