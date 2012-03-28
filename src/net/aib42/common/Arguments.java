package net.aib42.common;

public class Arguments {
	
	private static Arguments instance;
	
	private Arguments(){}
	
	/**
	 * Gets the single instance of Arguments.
	 *
	 * @return single instance of Arguments
	 */
	public static Arguments getInstance(){
		if(instance == null){
			instance = new Arguments();
		}
		return instance;
	}
	
	/**
	 * Gets the port number.
	 *
	 * @param args the args
	 * @return the port number
	 */
	public int getPortNumber(String[] args){
		if(args != null && args.length > 0){
			return Integer.parseInt(args[0]);
		}
		return Defaults.PORT_NUMBER;
	}
}
