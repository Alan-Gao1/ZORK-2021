package zork;

public class Weapon extends Item{
    private int damage;
    private int ammo;

    public void setDamage (String id){
        if(id.equals("bomb")){
          this.damage = 10000;
        }else if(id.equals("sword")){
          this.damage = 50;
        }else if(id.equals("dagger")){
          this.damage = 20;
        }else if(id.equals("mace")){
          this.damage = 25;
        }else if(id.equals("slingshot")){
          this.damage = 20;
        }
      }
    public void setAmmo (int ammo){
        this.ammo = ammo;
    }

    public Weapon(int weight, String name, boolean isOpenable, String id, String description, String startingRoom, int damage, int ammo){
        super(weight, name, isOpenable, id, description, startingRoom);
        this.damage = damage;
        this.ammo = ammo;
    }
}
