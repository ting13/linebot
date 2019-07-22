package app.bot.reqbody;

import java.math.BigDecimal;

public class Message {
	public String id;
	public String type;
	public String text;
	public ContentProvider contentProvider;
	public String  filename;
	public Number fileSize;
	public String title;
	public String address;
	public BigDecimal latitude;
	public BigDecimal longitude;
	public String packageId;
	public String stickerId;
	public Message() {
		// TODO Auto-generated constructor stub
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public Number getFileSize() {
		return fileSize;
	}
	public void setFileSize(Number fileSize) {
		this.fileSize = fileSize;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public BigDecimal getLatitude() {
		return latitude;
	}
	public void setLatitude(BigDecimal latitude) {
		this.latitude = latitude;
	}
	public BigDecimal getLongitude() {
		return longitude;
	}
	public void setLongitude(BigDecimal longitude) {
		this.longitude = longitude;
	}
	public String getPackageId() {
		return packageId;
	}
	public void setPackageId(String packageId) {
		this.packageId = packageId;
	}
	public String getStickerId() {
		return stickerId;
	}
	public void setStickerId(String stickerId) {
		this.stickerId = stickerId;
	}
}
