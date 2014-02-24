package com.miloshpetrov.sol2.game.input;

import com.badlogic.gdx.math.Vector2;
import com.miloshpetrov.sol2.common.SolMath;
import com.miloshpetrov.sol2.game.gun.GunItem;
import com.miloshpetrov.sol2.game.gun.GunMount;
import com.miloshpetrov.sol2.game.ship.SolShip;

public class Shooter {

  public static final float MAX_AD_TO_SHOOT = 15f;
  public static final float E_SPD_PERC = .3f;
  private boolean myShoot;
  private boolean myShoot2;
  private boolean myRight;
  private boolean myLeft;

  public Shooter() {
  }

  public void update(SolShip ship, Vector2 enemyPos, boolean dontRotate, boolean canShoot, Vector2 enemySpd,
    float shootDist)
  {
    myLeft = false;
    myRight = false;
    myShoot = false;
    myShoot2 = false;
    Vector2 shipPos = ship.getPos();
    if (enemyPos == null || !canShoot || shootDist < enemyPos.dst(shipPos)) return;

    GunItem g1 = processGun(ship, false);
    GunItem g2 = processGun(ship, true);
    if (g1 == null && g2 == null) return;

    float projSpd = 0;
    boolean prefSecond = false;
    if (g1 != null) {
      projSpd = g1.config.projConfig.spdLen;
    }
    if (g2 != null) {
      float g2PS = g2.config.projConfig.spdLen;
      if (projSpd < g2PS) {
        projSpd = g2PS;
        prefSecond = true;
      }
    }

    Vector2 gunRelPos = ship.getHull().getGunMount(prefSecond).getRelPos();
    Vector2 gunPos = SolMath.toWorld(gunRelPos, ship.getAngle(), shipPos);
    float shootAngle = calcShootAngle(gunPos, ship.getSpd(), enemyPos, enemySpd, projSpd);
    SolMath.free(gunPos);
    if (shootAngle != shootAngle) return;
    float shipAngle = ship.getAngle();
    if (SolMath.angleDiff(shootAngle, shipAngle) < MAX_AD_TO_SHOOT) {
      myShoot = true;
      myShoot2 = true;
      return;
    }

    if (dontRotate) return;
    Boolean ntt = Mover.needsToTurn(shipAngle, shootAngle, ship.getRotSpd(), ship.getRotAcc());
    if (ntt != null) {
      if (ntt) myRight = true; else myLeft = true;
    }
  }
  
  // returns gun if it's fixed & can shoot
  private GunItem processGun(SolShip ship, boolean second) {
    GunMount mount = ship.getHull().getGunMount(second);
    if (!mount.isFixed()) {
      if (second) myShoot2 = true; else myShoot = true;
      return null;
    }
    GunItem g = mount.getGun();
    return g != null && g.ammo > 0 ? g : null;
  }

  public boolean isShoot() {
    return myShoot;
  }

  public boolean isShoot2() {
    return myShoot2;
  }

  public boolean isLeft() {
    return myLeft;
  }

  public boolean isRight() {
    return myRight;
  }

  public static float calcShootAngle(Vector2 gunPos, Vector2 gunSpd, Vector2 ePos, Vector2 eSpd, float projSpd) {
    Vector2 eSpdShortened = SolMath.getVec(eSpd);
    eSpdShortened.scl(E_SPD_PERC);
    Vector2 relESpd = SolMath.distVec(gunSpd, eSpdShortened);
    SolMath.free(eSpdShortened);
    float rotAngle = SolMath.angle(relESpd);
    float v = relESpd.len();
    float v2 = projSpd;
    SolMath.free(relESpd);
    Vector2 toE = SolMath.distVec(gunPos, ePos);
    SolMath.rotate(toE, -rotAngle);
    float x = toE.x;
    float y = toE.y;
    float a = v * v - v2 * v2;
    float b = 2 * x * v;
    float c = x * x + y * y;
    float t = SolMath.genQuad(a, b, c);
    float res;
    if (t != t) {
      res = Float.NaN;
    } else {
      toE.x += t * v;
      res = SolMath.angle(toE) + rotAngle;
    }
    SolMath.free(toE);
    return res;
  }
}