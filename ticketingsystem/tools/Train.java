package ticketingsystem.tools;


import ticketingsystem.MyTicket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Train {
    private int seatNum;
    private int seatTotal;
    private int coachNum;
    private Seat[] seats;

    public Train(int seatNum, int coachNum, int stationNum) {
        this.seatNum = seatNum;
        this.coachNum = coachNum;
        this.seatTotal = seatNum * coachNum;
        this.seats = new Seat[seatTotal];
        for (int i = 0; i < seats.length; i++) {
            seats[i] = new Seat(stationNum);
        }
    }

    public void lock(MyTicket ticket) {
        int seatIdx = getSeatIdx(ticket.coach, ticket.seat);
        seats[seatIdx].lock();
    }

    public void occupy(MyTicket ticket) {
        int seatIdx = getSeatIdx(ticket.coach, ticket.seat);
        seats[seatIdx].occupy(ticket.departure, ticket.arrival);
    }

    public boolean isAvailable(MyTicket ticket) {
        int seatIdx = getSeatIdx(ticket.coach, ticket.seat);
        return seats[seatIdx].isAvailable(ticket);

    }

    public void unlock(MyTicket ticket) {
        int seatIdx = getSeatIdx(ticket.coach, ticket.seat);
        seats[seatIdx].unlock();
    }

    int getSeatIdx(int coach, int seat) {
        return (coach - 1) * seatNum + seat - 1;
    }

    public List<MyTicket> locateAvailables(int route, int departure, int arrival) {
        ArrayList<MyTicket> location = new ArrayList<>();
        for (int i = 1; i <= coachNum; i++) {
            for (int j = 1; j <= seatNum; j++) {
                MyTicket ticket = new MyTicket(route, i, j, departure, arrival);
                if (isAvailable(ticket)) {
                    location.add(ticket);
                }
            }
        }
        Collections.shuffle(location);
        return location;
    }

    public int countAvailables(int departure, int arrival) {
        int num = 0;
        MyTicket ticket = new MyTicket(0, 0, 0, departure, arrival);
        for (int i = 1; i <= coachNum; i++) {
            for (int j = 1; j <= seatNum; j++) {
                ticket.coach = i;
                ticket.seat = j;
                if (isAvailable(ticket)) {
                    num++;
                }
            }
        }
        return num;
    }

    public void free(MyTicket ticket) {
        int seatIdx = getSeatIdx(ticket.coach, ticket.seat);
        seats[seatIdx].free(ticket.departure, ticket.arrival);
    }
}
