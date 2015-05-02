package util;


public final class GLMath {
	/*public static final RealMatrix gluLookAt(Vector3D eye, Vector3D center, Vector3D up) {
		Vector3D f = center.subtract(eye).normalize();
		Vector3D s = Vector3D.crossProduct(f, up).normalize();
		Vector3D u = Vector3D.crossProduct(s, f);
		RealMatrix result = MatrixUtils.createRealMatrix(4, 4);
		result.setEntry(0, 0, s.getX());
		result.setEntry(1, 0, s.getY());
		result.setEntry(2, 0, s.getZ());
		result.setEntry(0, 1, u.getX());
		result.setEntry(1, 1, u.getY());
		result.setEntry(2, 1, u.getZ());
		result.setEntry(0, 2, -f.getX());
		result.setEntry(1, 2, -f.getY());
		result.setEntry(2, 2, -f.getZ());
		result.setEntry(3, 0, -Vector3D.dotProduct(s, eye));
		result.setEntry(3, 1, -Vector3D.dotProduct(u, eye));
		result.setEntry(3, 2, Vector3D.dotProduct(f, eye));
		return result;
	}

	public static void glRotatef(RealMatrix viewMatrix, int angle, Vector3D v) {
		double a = angle;
		double c = Math.cos(a);
		double s = Math.sin(a);
		Vector3D axis = v.normalize();
		Vector3D temp = axis.scalarMultiply(1.0 - c);
		RealMatrix rotate = MatrixUtils.createRealMatrix(4, 4);
		rotate.setEntry(0, 0, (c + temp.getX() * axis.getX()));
		rotate.setEntry(0, 1, (0 + temp.getX() * axis.getY() + s * axis.getZ()));
		rotate.setEntry(0, 2, (0 + temp.getX() * axis.getZ() - s * axis.getY()));
		rotate.setEntry(1, 0, (0 + temp.getY() * axis.getX() - s * axis.getZ()));
		rotate.setEntry(1, 1, (c + temp.getY() * axis.getY()));
		rotate.setEntry(1, 2, (0 + temp.getY() * axis.getZ() + s * axis.getX()));
		rotate.setEntry(2, 0, (0 + temp.getZ() * axis.getX() + s * axis.getY()));
		rotate.setEntry(2, 1, (0 + temp.getZ() * axis.getY() - s * axis.getX()));
		rotate.setEntry(2, 2, (c + temp.getZ() * axis.getZ()));
		RealMatrix result = MatrixUtils.createRealMatrix(4, 4);
		Result[0] = viewMatrix.getColumnVector(0).mapMultiply(rotate.getEntry(0, 0)) * rotate[0][0] + m[1] * rotate[0][1] + m[2] * rotate[0][2];
		Result[1] = m[0] * rotate[1][0] + m[1] * rotate[1][1] + m[2] * rotate[1][2];
		Result[2] = m[0] * rotate[2][0] + m[1] * rotate[2][1] + m[2] * rotate[2][2];
		Result[3] = m[3];
	}*/
}