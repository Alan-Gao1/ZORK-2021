package zork;

public class Weapon extends Item{
    private int damage;
    private int ammo;
    private boolean isWeapon;

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

    public int getDamage(){
        return damage;
    }

    public Weapon(int weight, String name, boolean isOpenable, String id, String description, String startingRoom, int damage, int ammo, boolean isWeapon){
        super(weight, name, isOpenable, id, description, startingRoom, isWeapon);
        this.damage = damage;
        this.ammo = ammo;
        this.isWeapon = isWeapon;
    }

    public boolean isWeapon(){
        return isWeapon;
    }
}
