package database;

public class Position {
	public Shop shop;
	public String position;
	public Position(Shop shop, String position) {
		this.shop = shop;
		this.position = position;
	}
	public String toString(){
		return shop.id+" "+shop.name+" "+position;
	}
}
