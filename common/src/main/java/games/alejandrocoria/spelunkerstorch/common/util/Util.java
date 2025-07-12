package games.alejandrocoria.spelunkerstorch.common.util;

import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Util {
    public static Quaternionf getRotation(Vec3 direction) {
        Vector3f baseDirection = new Vector3f(0, 0, 1);
        Vector3f targetDirection = new Vector3f((float) direction.x, (float) direction.y, (float) direction.z);
        float angleY = baseDirection.angleSigned(targetDirection, new Vector3f(0, 1, 0));
        float angleX = baseDirection.angleSigned(targetDirection.rotateY(-angleY), new Vector3f(1, 0, 0));
        Quaternionf rotation = new Quaternionf(new AxisAngle4f(angleY, 0, 1, 0));
        rotation.mul(new Quaternionf(new AxisAngle4f(angleX, 1, 0, 0)));
        return rotation;
    }

    public static Vector3f getNormal(Vector3f v1, Vector3f v2, Vector3f v3) {
        Vector3f a = new Vector3f();
        Vector3f b = new Vector3f();
        Vector3f dir = new Vector3f();
        v1.sub(v2, a);
        v1.sub(v3, b);
        a.cross(b, dir);
        return dir.normalize();
    }
}
