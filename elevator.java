// a thread that listens to requests
class RequestListener implements Runnable {
	private Elevator elevator;

	public RequestListener(Elevator e) {
		elevator = e;
	}
    @Override
    public void run() {
    	// ...
    	elevator.addXXRequest();
    }
}
class Elevator {

	private TreeSet<Request> upRequests = new TreeSet<Request>();
	private TreeSet<Request> downRequests = new TreeSet<Request>();
	private Thread requestListenerThread;

	// singleton
	private static Elevator elevator = null;
	public static Elevator getInstance() {
		if (elevator == null) {
			elevator = new Elevator();
			elevator.requestListenerThread = new Thread(new RequestListener(elevator), "RequestListenerThread");
		}
		return elevator;
	}

	public synchronized void addUpRequest(Reqest req) {
		upRequests.add(req);
	}

	public synchronized void addDownRequest(Reqest req) {
		downRequests.add(req);
	}

	public void goTo(Integer floor) {
		// move from current floor to destination floor
		currentFloor = floor;
		elevatorStatus = ElevatorStatus.STOPPED;
		openDoor();
		// wait for users to leave and enter
		elevatorStatus = ElevatorStatus.MOVING;
	}

	public void startElevator() {
		requestListenerThread.start();

		Integer nextStopFloor;
		while (true) {
			if (upRequests.isEmpty() && downRequests.isEmpty()) {
				elevatorStatus = ElevatorStatus.IDLE;
				continue;
			}
			elevatorStatus = ElevatorStatus.MOVING;
			// go all the way up
			while (!upRequests.isEmpty()) {
				synchronized(this) { Request req = upRequests.ceiling(new Request(currentFloor)); }
				goTo(req.stopFloor);
				synchronized(this) { upRequests.remove(req); } // clear internal/external requests at current floor
			}
			// go all the way down
			while (!downRequests.isEmpty()) {
				synchronized(this) { Request req = downRequests.floor(new Request(currentFloor)) };
				goTo(req.stopFloor);
				synchronized(this) { downRequests.remove(req); } // clear internal/external requests at current floor
			}
		}
	}
}