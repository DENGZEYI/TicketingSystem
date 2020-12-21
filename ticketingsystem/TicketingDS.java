package ticketingsystem;

import ticketingsystem.tools.BitHelper;
import ticketingsystem.tools.bitoniccounter.BitonicCounter;
import ticketingsystem.tools.Train;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class TicketingDS implements TicketingSystem {
    private int routeNum = 5;
    private int coachNum = 8;
    private int seatNum = 100;
    private int stationNum = 10;
    private int threadNum = 16;
    private BitonicCounter counter;
    private Train[] trains;
    private static final int fallbackThreshold = 10;


    private ConcurrentHashMap<Long, Ticket> record;

    public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
        this.routeNum = routeNum;
        this.coachNum = coachNum;
        this.seatNum = seatNum;
        this.stationNum = stationNum;
        this.threadNum = threadNum;
        //trains is a array containing Train objects
        this.trains = new Train[routeNum];
        for (int i = 0; i < trains.length; i++) {
            trains[i] = new Train(seatNum, coachNum, stationNum);
        }

        counter = new BitonicCounter((int) BitHelper.floor2power(threadNum));
        int mapCapacity = (int) (routeNum * coachNum * seatNum * stationNum * 0.5);
        record = new ConcurrentHashMap<>(mapCapacity, 0.75f, threadNum);
    }

    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        if (isIllegal(passenger, route, departure, arrival))
            return null;
        // Randomly choose a ticket
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        MyTicket ticket = new MyTicket(route, 0, 0, departure, arrival);
        for (int i = 0; i < fallbackThreshold; ++i) {
            ticket.coach = rand.nextInt(coachNum) + 1;
            ticket.seat = rand.nextInt(seatNum) + 1;
            if (tryBuyTicket(ticket))
                return constructTicket(passenger, ticket);
        }
        //get all available ticket into a List
        List<MyTicket> mayAvailables = trains[route - 1].locateAvailables(route, departure, arrival);
        for (MyTicket t : mayAvailables)
            if (tryBuyTicket(t))
                return constructTicket(passenger, t);
        return null;
    }

    public int inquiry(int route, int departure, int arrival) {
        if (isIllegal("INQUIRY", route, departure, arrival))
            return 0;
        return trains[route - 1].countAvailables(departure, arrival);
    }

    public boolean refundTicket(Ticket ticket) {
        if (ticket == null) {
            return false;
        }
        long tid = ticket.tid;
        MyTicket it = new MyTicket(ticket);
        if (isIllegal(ticket) || trains[ticket.route - 1].isAvailable(it) || !record.containsKey(tid))
            return false;
        if (!isEqual(ticket, record.get(tid)))
            return false;
        if (record.remove(tid) == null)
            return false;
        trains[ticket.route - 1].free(it);
        return true;
    }

    private boolean isEqual(Ticket a, Ticket b) {
        return a.tid == b.tid && a.passenger != null && b.passenger != null && a.passenger.equals(b.passenger)
                && a.route == b.route && a.coach == b.coach && a.seat == b.seat && a.departure == b.departure
                && a.arrival == b.arrival;
    }

    private boolean isIllegal(String passenger, int route, int departure, int arrival) {
        return passenger == null || passenger.equals("") || route <= 0 || route > routeNum || departure <= 0
                || departure > stationNum || arrival <= 0 || arrival > stationNum || departure >= arrival;
    }

    private boolean isIllegal(Ticket t) {
        String passenger = t.passenger;
        long tid = t.tid;
        int route = t.route;
        int coach = t.coach;
        int seat = t.seat;
        int departure = t.departure;
        int arrival = t.arrival;
        return tid < 0 || coach <= 0 || coach > coachNum || seat <= 0 || seat > seatNum
                || isIllegal(passenger, route, departure, arrival);
    }

    private Ticket constructTicket(String passenger, MyTicket ticket) {
        Ticket t = ticket.toTicket(counter.getNext(), passenger);
        record.put(t.tid, t);
        return t;
    }

    private boolean tryBuyTicket(MyTicket ticket) {
        int trainIdx = ticket.route - 1;
        if (trains[trainIdx].isAvailable(ticket)) {
            try {
                trains[trainIdx].lock(ticket);
                if (!trains[trainIdx].isAvailable(ticket))
                    return false;
                trains[trainIdx].occupy(ticket);
                return true;
            } finally {
                trains[trainIdx].unlock(ticket);
            }
        }
        return false;
    }
}
