/**
 * insert and update
 */
package org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import org.mpi.vasco.txstore.scratchpad.ScratchpadException;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDefDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation;
import org.mpi.vasco.util.debug.Debug;

public class DBTPCWShdDoCart2 extends DBShadowOperation {
	int shopping_id;
	int insert_i_id;
	Vector<Integer> up_id_v;
	Vector<Integer> up_q_v;
	Date access_time;

	public static DBTPCWShdDoCart2 createOperation(
			DataInputStream dis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		int shopping_id = dis.readInt();
		dos.writeInt(shopping_id);
		int insert_i_id = dis.readInt();
		dos.writeInt(insert_i_id);
		int vector_size = dis.readInt();
		dos.writeInt(vector_size);
		Vector<Integer> up_id_v = new Vector<Integer>();
		while(vector_size >0){
			int item_id = dis.readInt();
			up_id_v.add(new Integer(item_id));
			dos.writeInt(item_id);
			vector_size--;
		}
		vector_size = dis.readInt();
		dos.writeInt(vector_size);
		Vector<Integer> up_q_v = new Vector<Integer>();
		while(vector_size >0){
			int item_id = dis.readInt();
			up_q_v.add(new Integer(item_id));
			dos.writeInt(item_id);
			vector_size--;
		}
		long access_time = dis.readLong();
		dos.writeLong(access_time);
		return new DBTPCWShdDoCart2(baos.toByteArray(), shopping_id, insert_i_id,up_id_v, up_q_v, access_time);

	}

	public static DBTPCWShdDoCart2 createOperation(int shopping_id,
			int insert_i_id, Vector<Integer> up_id_v, Vector<Integer> up_q_v, long access_time) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		dos.writeByte(OP_SHADOWOP);
		dos.writeByte(OP_SHD_TPCW_DOCART2);
		dos.writeInt(shopping_id);
		dos.writeInt(insert_i_id);
		dos.writeInt(up_id_v.size());
		for(int i = 0; i < up_id_v.size();i++){
			dos.writeInt(up_id_v.get(i));
		}
		
		dos.writeInt(up_q_v.size());
		for(int i = 0; i < up_q_v.size();i++){
			dos.writeInt(up_q_v.get(i));
		}
	
		dos.writeLong(access_time);
		return new DBTPCWShdDoCart2(baos.toByteArray(), shopping_id, insert_i_id, up_id_v, up_q_v, access_time);

	}

	protected DBTPCWShdDoCart2(byte[] arr, int shopping_id,
			int insert_i_id, Vector<Integer> up_id_v, Vector<Integer> up_q_v,long access_time) {
		super(arr);
		this.shopping_id =shopping_id ; 
		this.insert_i_id = insert_i_id;
		this.up_id_v = up_id_v;
		this.up_q_v = up_q_v;
		this.access_time = new Date(access_time);

	}

	public DBTPCWShdDoCart2(byte[] arr) {
		super(arr);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void encode(DataOutputStream dos) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public int execute(IDatabase store) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void executeShadow(IDefDatabase iDefDatabase) {
		// TODO Auto-generated method stub
		
		//read cart from database
		String updateStr = "";
		ResultSet res = null;
		try {
			//res = iDefDatabase.executeQuery("SELECT scl_i_id FROM shopping_cart_line WHERE scl_sc_id = "+shopping_id + " AND _SP_del = 0 ");
			res = iDefDatabase.executeQuery("SELECT scl_i_id FROM shopping_cart_line WHERE scl_sc_id = "+shopping_id + " AND scl_i_id = " +insert_i_id);
			if(res.next()){
				updateStr = "UPDATE shopping_cart_line SET scl_qty = "+1+" WHERE scl_sc_id = "+shopping_id+" " +
				"AND scl_i_id = "+this.insert_i_id+" ";
			}else{
				Debug.println("item doesn't exist " + this.insert_i_id);
				updateStr = "INSERT into shopping_cart_line (scl_sc_id, scl_qty, scl_i_id) VALUES ("+shopping_id+","+1+","+this.insert_i_id+")";
			}
			res.close();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			Debug.println("select from shopping cart has some trouble");
			e1.printStackTrace();
		}
		
		try {
			iDefDatabase.executeUpdate(updateStr);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ScratchpadException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for(int i = 0; i < up_id_v.size(); i++){
			updateStr = "";
			Debug.println("item exists " + up_id_v.get(i));
			updateStr = "UPDATE shopping_cart_line SET scl_qty = "+up_q_v.get(i).intValue()+" WHERE scl_sc_id = "+shopping_id+" " +
				"AND scl_i_id = "+up_id_v.get(i).intValue()+" ";
			try {
				iDefDatabase.executeUpdate(updateStr);
			} catch( Exception e) {
				System.err.println("There was an exception when performing the shadow operation");
			}
		}
		
		try {
			iDefDatabase.executeUpdate("UPDATE shopping_cart SET sc_time = '"+access_time+"' WHERE sc_id = "+shopping_id);
		} catch( Exception e) {
			System.err.println("There was an exception when performing the shadow operation");
		}
		
	}

	@Override
	public boolean isQuery() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean registerIndividualOperations() {
		// TODO Auto-generated method stub
		return false;
	}

}
