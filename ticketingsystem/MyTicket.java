package ticketingsystem;

public class MyTicket {
    public int route, coach, seat, departure, arrival;

    public MyTicket(int route, int coach, int seat, int departure, int arrival) {
        this.route = route;
        this.coach = coach;
        this.seat = seat;
        this.departure = departure;
        this.arrival = arrival;
    }

    public MyTicket(Ticket t) {
        this.route = t.route;
        this.coach = t.coach;
        this.seat = t.seat;
        this.departure = t.departure;
        this.arrival = t.arrival;
    }
    
    public Ticket toTicket(long tid, String passenger) {
        Ticket t = new Ticket();
        t.tid = tid;
        t.passenger = passenger;
        t.route = route;
        t.coach = coach;
        t.seat = seat;
        t.departure = departure;
        t.arrival = arrival;
        return t;
    }
}