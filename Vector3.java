package Code;

public class Vector3 {
	float x = 0;
	float y = 0;
	float z = 0;
	
	public Vector3() {
		x = 0;
		y = 0;
		z = 0;
	}
	
	public Vector3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector3(double x, double y, double z) {
		this.x = (float)x;
		this.y = (float)y;
		this.z = (float)z;
	}
}
