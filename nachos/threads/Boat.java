package nachos.threads;

import javax.print.attribute.standard.MediaSize.NA;

import nachos.ag.BoatGrader;
//import sun.security.ec.point.Point;

public class Boat
{
    static BoatGrader bg;
    
    public static void selfTest()
    {
	BoatGrader b = new BoatGrader();
	
	System.out.println("\n ***Testing Boats with only 2 children***");
	begin(5, 7, b);

//	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
//  	begin(1, 2, b);

//  	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
//  	begin(3, 3, b);
    }

    public static void begin( int adults, int children, BoatGrader b )
    {
	// Store the externally generated autograder in a class
	// variable to be accessible by children.
	bg = b;

	// Instantiate global variables here
	
	// Create threads here. See section 3.4 of the Nachos for Java
	// Walkthrough linked from the projects page.
	sharedLock = new Lock();
	AinM = new Condition2(sharedLock);
	AinO = new Condition2(sharedLock);
	CinM = new Condition2(sharedLock);
	CinO = new Condition2(sharedLock);
	Pilot = new Condition2(sharedLock);
	isDoneS = new Semaphore(0);
	//isDone = false;
	boatPosition = true;
	peopleOnBoat = 0;
	NAinM = 0;
	NAinO = adults;
	NCinM = 0;
	NCinO = children;

	for (int i = 0; i < adults; i++){
		KThread Atmp = new KThread(new Runnable(){
			public void run() {
				AdultItinerary();
			}
		});
		Atmp.setName("Adult: " + i);
		Atmp.fork();
	}

	for (int i = 0; i < children; i++){
		KThread Ctmp = new KThread(new Runnable(){
			public void run() {
				ChildItinerary();
			}
		});
		Ctmp.setName("Child: " + i);
		Ctmp.fork();
	}
	isDoneS.P();


    }

    static void AdultItinerary()
    {
	bg.initializeAdult();   // Required for autograder interface. Must be the first thing called.
	boolean myPosition = true;
	sharedLock.acquire();	// Short busy waiting for condition lock
	while(true){
		//System.out.println("0");
		if (myPosition){
			//System.out.println("1");
			if (!boatPosition){
				//System.out.println("2");
				CinM.wakeAll();
				AinO.sleep();
			}
			else{
				if (peopleOnBoat != 0){
					CinO.wakeAll();
					AinO.sleep();
				}
				else if (NCinO <= 1){
					boatPosition = false;
					peopleOnBoat = 0;
					myPosition = false;
					NAinO -= 1;
					NAinM += 1;
					bg.AdultRowToMolokai();
					CinM.wakeAll();
					AinM.sleep();
				}
				else{
					CinO.wakeAll();
					AinO.sleep();
				}
			}
		}
		else{
			AinM.sleep();
		}
	}
	//sharedLock.release();
    }

    static void ChildItinerary()
    {
	bg.initializeChild(); //Required for autograder interface. Must be the first thing called.
	boolean myPosition = true;
	sharedLock.acquire();	// Short busy waiting for condition lock
	while(true){
		if (myPosition){
			if (!boatPosition){
				CinM.wakeAll();
				CinO.sleep();
			}
			else{
				if (peopleOnBoat == 0){
					if (NCinO <= 1 && NAinO >= 1){
						AinO.wakeAll();
						CinO.sleep();
					}
					else{
						peopleOnBoat = 1;
						CinO.wakeAll();
						if (NCinO == 1){
							// Done
							boatPosition = false;
							peopleOnBoat = 0;
							myPosition = false;
							NCinO -= 1;
							NCinM += 1;
							bg.ChildRowToMolokai();
							isDoneS.V();
							//isDone = true;
							CinM.sleep();
						}
						else{
							Pilot.sleep();
							boatPosition = false;
							peopleOnBoat = 0;
							myPosition = false;
							NCinO -= 2;
							NCinM += 2;
							bg.ChildRowToMolokai();
							bg.ChildRideToMolokai();
							if (NCinO == 0 && NAinO == 0){
								isDoneS.V();
								//isDone = true;
								CinM.sleep();
							}
							else{
								CinM.wakeAll();
								CinM.sleep();
							}
						}
					}
				}
				else if (peopleOnBoat == 1){
					peopleOnBoat = 2;
					Pilot.wakeAll();
					myPosition = false;
					CinM.sleep();
				}
				else{
					Pilot.wakeAll();
					CinO.sleep();
				}
			}
		}
		else{
			if (boatPosition){
				CinO.wakeAll();
				AinO.wakeAll();
				CinM.sleep();
			}
			else{
				if (peopleOnBoat == 0){
					// Something went wrong
					boatPosition = true;
					peopleOnBoat = 0;
					myPosition = true;
					NCinM -= 1;
					NCinO += 1;
					bg.ChildRowToOahu();
					//bg.ChildRowToMolokai();
					CinO.wakeAll();
					AinO.wakeAll();
					CinO.sleep();
				}
				else{
					CinM.sleep();
				}
			}
		}
	}
	//sharedLock.release();
    }

    static void SampleItinerary()
    {
	// Please note that this isn't a valid solution (you can't     KThread.sleep();
	bg.ChildRideToMolokai();
	bg.AdultRideToMolokai();
	bg.ChildRideToMolokai();
	}
	// shared variable
	public static Condition2 AinO, AinM, CinO, CinM, Pilot;
	public static Lock sharedLock;
	public static boolean boatPosition;		// true: Oahu  false: Molokai
	public static int peopleOnBoat;			
	public static Semaphore isDoneS;
	//public static boolean isDone = false;
	public static int NCinO, NCinM, NAinO, NAinM;
}
