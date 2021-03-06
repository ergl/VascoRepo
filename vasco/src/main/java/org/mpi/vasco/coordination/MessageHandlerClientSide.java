/*
 * This class is doing x;
 * Created by @Creator on @Date
 */
package org.mpi.vasco.coordination;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.RandomStringUtils;
import org.mpi.vasco.coordination.membership.Role;
import org.mpi.vasco.coordination.protocols.AsymProtocol;
import org.mpi.vasco.coordination.protocols.messages.CleanUpBarrierMessage;
import org.mpi.vasco.coordination.protocols.messages.LockRepMessage;
import org.mpi.vasco.coordination.protocols.messages.LockReqMessage;
import org.mpi.vasco.coordination.protocols.messages.MessageFactory;
import org.mpi.vasco.coordination.protocols.messages.MessageTags;
import org.mpi.vasco.coordination.protocols.util.LockReply;
import org.mpi.vasco.coordination.protocols.util.LockRequest;
import org.mpi.vasco.coordination.protocols.util.Protocol;
import org.mpi.vasco.network.ParallelPassThroughNetworkQueue;
import org.mpi.vasco.network.messages.MessageBase;
import org.mpi.vasco.network.netty.NettyTCPReceiver;
import org.mpi.vasco.network.netty.NettyTCPSender;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.util.debug.Debug;
import org.mpi.vasco.util.debug.PerProfile;

//:TODO: think about how to recycle the used messages, since some components of
// messaages like lockreply or lockrequest are continiously used by the other parts of code

/**
 * The Class MessageHandlerClientSide.
 */
public class MessageHandlerClientSide extends BaseNode{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4040510651915229397L;
	
	/** The mf. */
	private MessageFactory mf;
	
	/** The agent. */
	private VascoServiceAgent agent;

	/**
	 * Instantiates a new message handler client side.
	 *
	 * @param membershipFile the membership file
	 * @param myRole the my role
	 * @param myId the my id
	 */
	public MessageHandlerClientSide(String membershipFile, Role myRole, int myId) {
		super(membershipFile, myRole, myId);
		mf = new MessageFactory();
		System.out.printf("Set up lock client %d for lock server\n", myId);
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.network.ByteHandler#handle(byte[])
	 */
	@Override
	public void handle(byte[] bytes) {
		MessageBase msg = mf.fromBytes(bytes);
		if (msg == null) {
			throw new RuntimeException("Should never receive a null message");
		}
		
		switch (msg.getTag()) {
		case MessageTags.LOCKREP:
			//Receive the reply from the centralized server or a client
			process((LockRepMessage) msg);
			break;
		case MessageTags.LOCKREQ:
			//Barrier, receive request from the client
			process((LockReqMessage) msg);
			break;
		case MessageTags.CLEANUPBARRIER:
			//Barrier, clean up a local barrier on the behalf of the remote peer
			process((CleanUpBarrierMessage)msg);
			break;
		default:
			throw new RuntimeException("invalid message tag: " + msg.getTag());
		}
		
	}

	/**
	 * Process.
	 *
	 * @param msg the msg
	 */
	private void process(LockRepMessage msg) {
		//Debug.printf("Receive a lock reply message from server or client content %s \n", msg.toString());
		LockReply lcReply = msg.getLockRly();
		int pType = lcReply.getProtocolType();
		//get either sym or asym protocol
		Protocol p = this.getAgent().getProtocol(pType);
		if(p == null){
			throw new RuntimeException("No such a protocol " + lcReply.getProtocolType());
		}
		p.addLockReply(msg.getProxyTxnId(), lcReply);
		//mf.returnLockRepMessage(msg);
		
	}
	
	/**
	 * Process.
	 *
	 * @param msg the msg
	 */
	private void process(LockReqMessage msg){
		//Debug.printf("Receive a lock request message from client content %s \n", msg.toString());
		Protocol p = this.getAgent().getProtocol(Protocol.PROTOCOL_ASYM);
		if(p == null){
			throw new RuntimeException("No such a protocol " + Protocol.PROTOCOL_ASYM);
		}
		
		LockReply lcReply = p.getLocalPermission(msg.getProxyTxnId(), msg.getLockReq());
		LockRepMessage repMsg = new LockRepMessage(msg.getProxyTxnId(), lcReply);
		int clientId = msg.getGlobalProxyId();
		this.sendToLockClient(repMsg, clientId);
		//mf.returnLockReqMessage(msg);
		//mf.returnLockRepMessage(repMsg);
	}
	
	/**
	 * Process.
	 *
	 * @param msg the msg
	 */
	private void process(CleanUpBarrierMessage msg){
		//Debug.printf("Receive a clean up barrier message from client content %s\n", msg.toString());
		Protocol p = this.getAgent().getProtocol(Protocol.PROTOCOL_ASYM);
		if(p == null){
			throw new RuntimeException("No such a protocol " + Protocol.PROTOCOL_ASYM);
		}
		
		LockRequest lcr = ((AsymProtocol) p).getAsymRequestMap().get(msg.getProxyTxnId());
		p.cleanUpLocal(msg.getProxyTxnId(), lcr.getKeyList(), lcr.getOpName());
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.coordination.BaseNode#setUp()
	 */
	@Override
	public void setUp() {
		// set up for outgoing messages
		NettyTCPSender sendNet = new NettyTCPSender();
		this.setSender(sendNet);
		sendNet.setTCPNoDelay(false);
		sendNet.setKeepAlive(true);
				
		int threadCount = 2;
		ParallelPassThroughNetworkQueue ptnq = new ParallelPassThroughNetworkQueue(
						this, threadCount);
		NettyTCPReceiver rcv = new NettyTCPReceiver(this.getMembership().getMe()
						.getInetSocketAddress(), ptnq, threadCount);
	}
	
	//for testing the functionalities
	/**
	 * Send messages.
	 *
	 * @param n the n
	 */
	public void sendMessages(int n){
		for(int i = 0; i < n; i++){
			LockRequest lR = new LockRequest("a");//bit flip to a or b
			LockReqMessage msg = new LockReqMessage(new ProxyTxnId(0,this.getMyId(),i),
					this.getMyId(), lR);
			this.sendToLockServer(msg);
			mf.returnLockReqMessage(msg);
		}
	}
	
	/** The counter per client. */
	static int counterPerClient = 0;
	
	/**
	 * Send test sym request message.
	 */
	public void sendTestSymRequestMessage(){
		PerProfile.startMeasure();
		Debug.println("Send a test sym request message to server\n");
		String opName = this.getAgent().getConfTable().getRandomConflictOpNameByType(Protocol.PROTOCOL_SYM);
		ProxyTxnId txnId = new ProxyTxnId(this.getMyId(), 0, counterPerClient++);
		LockRequest lr = new LockRequest(opName);
		//Random random = new Random();
		int numOfKeys = 5;//random.nextInt(5);
		Debug.printf("Generate %d keys\n", numOfKeys);
		for(int i = 0; i < numOfKeys; i++){
			String keyStr = "a_" + i;//RandomStringUtils.randomAlphabetic(5).toLowerCase();
			if(keyStr.equalsIgnoreCase("")){
				throw new RuntimeException("You generated an empty string\n");
			}
			lr.addKey(keyStr);
		}
	    this.agent.getProtocol(Protocol.PROTOCOL_SYM).getPermission(txnId, lr);
		PerProfile.endMeasure();
	}
	
	/**
	 * Send test sym request message in batch.
	 *
	 * @param batchSize the batch size
	 */
	public void sendTestSymRequestMessageInBatch(int batchSize){
		if(batchSize <= 0){
			throw new RuntimeException("batch size must be positive");
		}
		while(batchSize > 0){
			this.sendTestSymRequestMessage();
			batchSize--;
		}
	}
	
	public void sendTestSymRequestMessageEveryEpochInBatch(int waitTime, int batchSize){
		if(batchSize <= 0){
			throw new RuntimeException("batch size must be positive");
		}
		Random random = new Random();
		while(batchSize > 0){
			System.out.println("the remaining messages are " + batchSize);
			this.sendTestSymRequestMessage();
			try {
				int randomWaitTime = random.nextInt(waitTime) + 1;
				Thread.sleep(randomWaitTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			batchSize--;
		}
	}
	
	/**
	 * Send test a sym request message.
	 */
	public void sendTestASymRequestMessage(){
		PerProfile.startMeasure();
		String opName = this.getAgent().getConfTable().getRandomConflictOpNameByType(Protocol.PROTOCOL_ASYM);
		ProxyTxnId txnId = new ProxyTxnId(this.getMyId(), 0, counterPerClient++);
		LockRequest lr = new LockRequest(opName);
		int numOfKeys = 5;//random.nextInt(5);
		//Debug.printf("Generate %d keys\n", numOfKeys);
		for(int i = 0; i < numOfKeys; i++){
			String keyStr = "a_" + i;//RandomStringUtils.randomAlphabetic(5).toLowerCase();
			if(keyStr.equalsIgnoreCase("")){
				throw new RuntimeException("You generated an empty string\n");
			}
			lr.addKey(keyStr);
		}
		Debug.printf("Generate a test asym request %s\n", lr.toString());
		LockReply lcReply = this.agent.getProtocol(Protocol.PROTOCOL_ASYM).getPermission(txnId, lr);
		PerProfile.endMeasure();
		Debug.printf("sendTestASymRequestMessage lcReply received at the end%s\n", lcReply.toString());
		this.getAgent().cleanUpLocalOperation(txnId, lr);
	}
	
	/**
	 * Send test a sym request message in batch.
	 *
	 * @param batchSize the batch size
	 */
	public void sendTestASymRequestMessageInBatch(int batchSize){
		if(batchSize <= 0){
			throw new RuntimeException("batch size must be positive");
		}
		while(batchSize > 0){
			this.sendTestASymRequestMessage();
			batchSize--;
		}
	}
	
	/**
	 * Test.
	 */
	public void test(){
		System.out.println("Test the client and server");
		Scanner keyboard = new Scanner(System.in);
		while(true){
			System.out.println("[1] send a sym msg, [2] send a batch of sym, [3] send a batch of sym and every msg for an epoch, [4]send a asym msg, [5] send a batch of asym, [6] quit"+ "\n");
			int input=keyboard.nextInt();
			switch(input){
			case 1:
				//test the full functionality of the lock service
				//send symtry message
				this.sendTestSymRequestMessage();
				break;
			case 2:
				int batchSize1 = keyboard.nextInt();
				System.out.println("Send sym message in batch " + batchSize1);
				this.sendTestSymRequestMessageInBatch(batchSize1);
				break;
			case 3:
				int waitTime = keyboard.nextInt();
				System.out.println("Send a sym message every " + waitTime + " ms");
				int batchSize2 = keyboard.nextInt();
				System.out.println("Send sym message in batch " + batchSize2);
				this.sendTestSymRequestMessageEveryEpochInBatch(waitTime, batchSize2);
				break;
			case 4:
				this.sendTestASymRequestMessage();
				break;
			case 5:
				int batchSize3 = keyboard.nextInt();
				System.out.println("Send asym message in batch " + batchSize3);
				this.sendTestASymRequestMessageInBatch(batchSize3);
				break;
			case 6:
				keyboard.close();
				return;
			default:
				keyboard.close();
				throw new RuntimeException("Not specified yet");
			}
		}
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args){
		if(args.length != 2){
			System.out.println("MessageHandlerServerSide [memshipFile] [id]");
			System.exit(-1);
		}
		
		String membershipFile = args[0];
		int myId = Integer.parseInt(args[1]);
		
		VascoServiceAgent vsAgent = VascoServiceAgentFactory.createVascoServiceAgent(membershipFile, myId);
		
		MessageHandlerClientSide mClient = vsAgent.getClient();
	
		System.out.println("Client is set up and ready for testing");
		mClient.test();
	}

	/**
	 * Gets the agent.
	 *
	 * @return the agent
	 */
	public VascoServiceAgent getAgent() {
		return agent;
	}

	/**
	 * Sets the agent.
	 *
	 * @param agent the new agent
	 */
	public void setAgent(VascoServiceAgent agent) {
		this.agent = agent;
	}

}
