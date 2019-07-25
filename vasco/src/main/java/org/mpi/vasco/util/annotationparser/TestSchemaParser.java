package org.mpi.vasco.util.annotationparser;

import org.mpi.vasco.util.annotationparser.SchemaParser;

public class TestSchemaParser {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		if(args.length != 1) {
			System.out.println("Correct Usage: java -jar fileName");
			System.exit(-1);
		}

		String sqlFile = args[0];
		SchemaParser sP = new SchemaParser(sqlFile);
		sP.parseAnnotations();
		sP.printOut();
	}

}
