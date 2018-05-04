package main;

import model.StateHolder;
import view.GUI;

public class Main {

	public static void main(String[] args) {
		
		StateHolder state = new StateHolder();
		
		new GUI(state);
		
	} // END OF MAIN
	
}
