package model;

public class Alarm {
	private String description;
	private String nodeIp;
	private int id;
	private String time;
	
	public Alarm(String description, String nodeIp, int id, String time) {
		super();
		this.description = description;
		this.nodeIp = nodeIp;
		this.id = id;
		this.time = time;
	}
	public String getDescription() {
		return description;
	}
	public String getNodeIp() {
		return nodeIp;
	}
	public int getId() {
		return id;
	}
	public String getTime() {
		return time;
	}
	@Override
	public String toString() {
		return "Alarm [description=" + description + ", nodeIp=" + nodeIp + ", id=" + id + ", time=" + time + "]";
	}

}
