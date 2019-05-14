package Code;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/*
 * 
 * This Class implements Catmull-Clark Subdivision.
 * In order for this code to run, the Vector3 class must be defined.
 * Algorithm source: https://en.wikipedia.org/wiki/Catmull-Clark_subdivision_surface
 * 
 */
public class CatmullClark {
	// This variable is reassigned every time SubdivisionSurface is called.
	// Its sole purpose is to decrease the length of function calls.
	private static Vector3[] meshVertices;
	
	/**
	 * 
	 * This function just runs some test data to make sure the class works.
	 * Consider it a unit test that would get removed later.
	 * 
	 * This function returns nothing, but prints some stuff.
	 * 
	 * @param args	N/A
	 * 
	 */
	public static void main(String[] args) {
		// origVertices is composed of two triangles that form the unit square.
		// If replacing this code, be aware that each 3 vertices corresponds to
		// a face and that each vertex should be listed clock-wise.
		Vector3[] origVertices = new Vector3[6];
		origVertices[0] = new Vector3(0, 0, 0);
		origVertices[1] = new Vector3(1, 0, 0);
		origVertices[2] = new Vector3(1, 1, 0);
		origVertices[3] = new Vector3(0, 0, 0);
		origVertices[4] = new Vector3(1, 1, 0);
		origVertices[5] = new Vector3(0, 1, 0);
		
		Vector3[] smoothVertices = SubdivisionSurface(origVertices);
		
		Print(origVertices, 16, 16);
		Print(smoothVertices, 16, 16);
		
		// Expected values for this test.
		// Source: did the math by hand.
		Vector3[] targetVertices = new Vector3[36];
		targetVertices[0] = new Vector3(0.6666, 0.6666, 0.6666);
		targetVertices[1] = new Vector3(1.6666, 0.8333, 0.0);
		targetVertices[2] = new Vector3(0.5, 0.0, 0.0);
		targetVertices[3] = new Vector3(0.6666, 0.6666, 0.6666);
		targetVertices[4] = new Vector3(0.5, 0.5, 0.0);
		targetVertices[5] = new Vector3(1.6666, 0.8333, 0.0);
		targetVertices[6] = new Vector3(0.6666, 0.6666, 0.6666);
		targetVertices[7] = new Vector3(0.1666, -0.6666, 0.0);
		targetVertices[8] = new Vector3(0.5, 0.5, 0.0);
		targetVertices[9] = new Vector3(0.6666, 0.6666, 0.6666);
		targetVertices[10] = new Vector3(1.0, 0.5, 0.0);
		targetVertices[11] = new Vector3(0.1666, -0.6666, 0.0);
		targetVertices[12] = new Vector3(0.6666, 0.6666, 0.6666);
		targetVertices[13] = new Vector3(0.1666, 0.8333, 0.0);
		targetVertices[14] = new Vector3(1.0, 0.5, 0.0);
		targetVertices[15] = new Vector3(0.6666, 0.6666, 0.6666);
		targetVertices[16] = new Vector3(0.5, 0.0, 0.0);
		targetVertices[17] = new Vector3(0.1666, 0.8333, 0.0);
		targetVertices[18] = new Vector3(0.3333, 0.3333, 0.3333);
		targetVertices[19] = new Vector3(0.8333, 1.6666, 0.0);
		targetVertices[20] = new Vector3(0.5, 0.5, 0.0);
		targetVertices[21] = new Vector3(0.3333, 0.3333, 0.3333);
		targetVertices[22] = new Vector3(0.0, 0.5, 0.0);
		targetVertices[23] = new Vector3(0.8333, 1.6666, 0.0);
		targetVertices[24] = new Vector3(0.3333, 0.3333, 0.3333);
		targetVertices[25] = new Vector3(0.8333, 0.1666, 0.0);
		targetVertices[26] = new Vector3(0.0, 0.5, 0.0);
		targetVertices[27] = new Vector3(0.3333, 0.3333, 0.3333);
		targetVertices[28] = new Vector3(0.5, 1.0, 0.0);
		targetVertices[29] = new Vector3(0.8333, 0.1666, 0.0);
		targetVertices[30] = new Vector3(0.3333, 0.3333, 0.3333);
		targetVertices[31] = new Vector3(-0.6666, 0.1666, 0.0);
		targetVertices[32] = new Vector3(0.5, 1.0, 0.0);
		targetVertices[33] = new Vector3(0.3333, 0.3333, 0.3333);
		targetVertices[34] = new Vector3(0.5, 0.5, 0.0);
		targetVertices[35] = new Vector3(-0.6666, 0.1666, 0.0);
		
		// Calculate the Manhattan distance error.
		float error = 0;
		if(smoothVertices.length == targetVertices.length) {
			for(int i = 0; i < smoothVertices.length; i ++) {
				float dX = smoothVertices[i].x - targetVertices[i].x;
				float dY = smoothVertices[i].y - targetVertices[i].y;
				float dZ = smoothVertices[i].z - targetVertices[i].z;
				dX = dX < 0 ? -dX: dX;
				dY = dY < 0 ? -dY: dY;
				dZ = dZ < 0 ? -dZ: dZ;
				error += dX + dY + dZ;
			}
			
			System.out.println("Total deviation from expected values is less than: " + error + ".");
		}
		else {
			System.out.println("Smoothing failed: results were not as they were expected.");
		}
	}
	
	/**
	 * 
	 * This function displays a "mesh". It first prints every vertex and
	 * then displays a simple ASCII style grid.
	 * 
	 * This function returns nothing, but prints some stuff.
	 * 
	 * @param points		list of vertices to display
	 * @param resolution	resolution of the display
	 * @param scale			scale of the display
	 * 
	 */
	private static void Print(Vector3[] points, int resolution, float scale) {
		// Print out every vertex first for debugging.
		for(int i = 0; i < points.length; i ++) {
			System.out.println("Vertex " + i + ": " + points[i].x + " " + points[i].y + " " + points[i].z);
		}
		
		// Create the ASCII style grid.
		for(int x = 0; x < resolution + 1; x ++) {
			for(int y = 0; y < resolution + 1; y ++) {
				String nextSymbol = "   ";
				
				// Check if any point in points is closer than half
				// of a grid unit away from the grid point we are
				// printing. If so, change the grid point from
				// a " " to an "X".
				for(int i = 0; i < points.length; i ++) {
					float dX = points[i].x - x / scale;
					float dY = points[i].y - y / scale;
					if(dX * dX + dY * dY < 1 / (scale * scale) / 4) {
						nextSymbol = " X ";
						break;
					}
				}
				
				System.out.print(nextSymbol);
			}
			
			System.out.println();
		}
		
		System.out.println();
	}
	
	/**
	 * 
	 * This function takes a list of Vector3 points (which
	 * represent a mesh) and returns a list of Vector3
	 * points (which represent a smoother mesh).
	 * 
	 * @param meshVertices	1D array of vertices
	 * 						(non-reusable)
	 * 
	 * @return				1D array of vertices
	 * 						(6x the in length)
	 * 
	 */
	public static Vector3[] SubdivisionSurface(Vector3[] newMeshVertices) {
		// This dictionary gives a list of all triangles that contain a given point.
		Map<Vector3, int[]> vertexTriangles = new Hashtable<Vector3, int[]>();
		meshVertices = newMeshVertices;
		
		// This loop populates vertexTriangles.
		for(int i = 0; i < meshVertices.length; i ++) {
			// Elements contains all previously found triangles.
			int[] elements = vertexTriangles.get(meshVertices[i]);
			if(elements != null && 0 < elements.length) {
				// Check that this is a new triangle.
				boolean newElement = true;
				for(int j = 0; j < elements.length; j ++) {
					if(elements[j] == i-i%3) {
						newElement = false;
					}
				}
				
				// If this is a new triangle, update the dictionary.
				if(newElement) {
					int[] newElements = new int[elements.length + 1];
					newElements[0] = i-i%3;
					
					for(int j = 0; j < elements.length; j ++) {
						newElements[j+1] = elements[j];
					}
					
					vertexTriangles.put(meshVertices[i], newElements);
				}
			}
			else {
				// If no previous triangles are found, just add this one.
				vertexTriangles.put(meshVertices[i], new int[]{i-i%3});
			}
		}
		
		/*
		for(int i = 0; i < meshVertices.length; i ++) {
			int[] tris = vertexTriangles.get(meshVertices[i]);
			for(int j = 0; j < tris.length; j ++) {
				System.out.println(tris[j]);
			}
		}
		*/
		
		// Create an output array.
		Vector3[] vertices = new Vector3[meshVertices.length * 6];
		
		for(int i = 0;i < meshVertices.length; i += 3){
			// Find the average of the triangle's vertices.
			Vector3 facePoint = new Vector3();
			facePoint.x = (meshVertices[i].x + meshVertices[i+1].x + meshVertices[i+2].x) / 3;
			facePoint.y = (meshVertices[i].x + meshVertices[i+1].x + meshVertices[i+2].x) / 3;
			facePoint.z = (meshVertices[i].x + meshVertices[i+1].x + meshVertices[i+2].x) / 3;
			
			// Face 1.
			vertices[i * 6] = facePoint;
			vertices[i * 6 + 1] = Barycenter(
					meshVertices[i],
					vertexTriangles.get(meshVertices[i])
					);
			vertices[i * 6 + 2] = EdgePoint(
					meshVertices[i],
					meshVertices[i+1],
					vertexTriangles.get(meshVertices[i]),
					vertexTriangles.get(meshVertices[i+1])
					);
			
			// Face 2.
			vertices[i * 6 + 3] = facePoint;
			vertices[i * 6 + 4] = EdgePoint(
					meshVertices[i],
					meshVertices[i+2],
					vertexTriangles.get(meshVertices[i]),
					vertexTriangles.get(meshVertices[i+2])
					);
			vertices[i * 6 + 5] = Barycenter(
					meshVertices[i],
					vertexTriangles.get(meshVertices[i])
					);
			
			// Face 3.
			vertices[i * 6 + 6] = facePoint;
			vertices[i * 6 + 7] = Barycenter(
					meshVertices[i+2],
					vertexTriangles.get(meshVertices[i+2])
					);
			vertices[i * 6 + 8] = EdgePoint(
					meshVertices[i],
					meshVertices[i+2],
					vertexTriangles.get(meshVertices[i]),
					vertexTriangles.get(meshVertices[i+2])
					);
			
			// Face 4.
			vertices[i * 6 + 9] = facePoint;
			vertices[i * 6 + 10] = EdgePoint(
					meshVertices[i+1],
					meshVertices[i+2],
					vertexTriangles.get(meshVertices[i+1]),
					vertexTriangles.get(meshVertices[i+2])
					);
			vertices[i * 6 + 11] = Barycenter(
					meshVertices[i+2],
					vertexTriangles.get(meshVertices[i+2])
					);
			
			// Face 5.
			vertices[i * 6 + 12] = facePoint;
			vertices[i * 6 + 13] = Barycenter(
					meshVertices[i+1],
					vertexTriangles.get(meshVertices[i+1])
					);
			vertices[i * 6 + 14] = EdgePoint(
					meshVertices[i+1],
					meshVertices[i+2],
					vertexTriangles.get(meshVertices[i+1]),
					vertexTriangles.get(meshVertices[i+2])
					);
			
			// Face 6.
			vertices[i * 6 + 15] = facePoint;
			vertices[i * 6 + 16] = EdgePoint(
					meshVertices[i],
					meshVertices[i+1],
					vertexTriangles.get(meshVertices[i]),
					vertexTriangles.get(meshVertices[i+1])
					);
			vertices[i * 6 + 17] = Barycenter(
					meshVertices[i+1],
					vertexTriangles.get(meshVertices[i+1])
					);
		}
		
		return vertices;
	}
	
	/**
	 * 
	 * This function takes in an edge and returns its edge point,
	 * as defined by Catmull-Clark. See the link at the top of
	 * this class for more details.
	 * 
	 * @param v0		Vertex 0 of the edge
	 * @param v1		Vertex 1 of the edge
	 * @param v0Tris	Array of triangles containing v0
	 * @param v1Tris	Array of triangles containing v1
	 * 
	 * @return			Each edge point is the average of the two vertices that define it
	 * 					and the face points of the two faces that it is part of
	 * 
	 */
	private static Vector3 EdgePoint(Vector3 v0, Vector3 v1, int[] v0Tris, int[] v1Tris) {
		// The first step is to find the faces that have v0->v1 as an edge.
		// t0 is the first such triangle, t1 is the last.
		// triCount is the number of such triangles.
		int t0 = 0;
		int t1 = 0;
		int triCount = 0;
		for(int i = 0; i < v0Tris.length; i ++) {
			for(int j = 0; j < v1Tris.length; j ++) {
				// If two of the indices in v0Tris and v1Tris are the same,
				// that means we found a face that has v0->v1 as an edge.
				if(v0Tris[i] == v1Tris[j]) {
					if(triCount == 0) {
						t0 = v0Tris[i];
					}
					else {
						t1 = v1Tris[j];
					}
					
					triCount += 1;
				}
			}
		}
		
		// The most common situation is that each edge has two face connected to it.
		// The only exceptions are meshes employing bad practices (pinch points
		// lead to too many faces, holes lead to too few).
		// This is the result per Catmull-Clark.
		if(triCount == 2){
			Vector3 result=new Vector3();
			result.x = (v0.x + v1.x + (meshVertices[t0].x + meshVertices[t0+1].x + meshVertices[t0+2].x) / 3 + (meshVertices[t1].x + meshVertices[t1+1].x + meshVertices[t1+2].x) / 3) / 4;
			result.y = (v0.y + v1.y + (meshVertices[t0].y + meshVertices[t0+1].y + meshVertices[t0+2].y) / 3 + (meshVertices[t1].y + meshVertices[t1+1].y + meshVertices[t1+2].y) / 3) / 4;
			result.z = (v0.z + v1.z + (meshVertices[t0].z + meshVertices[t0+1].z + meshVertices[t0+2].z) / 3 + (meshVertices[t1].z + meshVertices[t1+1].z + meshVertices[t1+2].z) / 3) / 4;
			return result;
		}
		
		// If the number of faces is too low or too high, we treat the edge as a sharp edge
		// and return a result that is the average of only the two edge vertices. That way
		// the original meshes shape at the region is maintained (since Catmull-Clark
		// can't smooth it).
		Vector3 result = new Vector3();
		result.x = (v0.x + v1.x) / 2;
		result.y = (v0.y + v1.y) / 2;
		return result;
	}
	
	/**
	 * 
	 * This function takes in a vertex and returns its barycenter,
	 * as defined by Catmull-Clark. For more details, visit the
	 * link at the top of this class.
	 * 
	 * @param v			A vertex we want to find the barycenter of
	 * @param vTris		Array of triangles containing v
	 * 
	 * @return			The barycenter of v
	 * 
	 */
	private static Vector3 Barycenter(Vector3 v, int[] vTris){
		// fp is the average of all connected face points
		// ep is the average of all connected edge midpoints
		// edges is a list of all connected edges
		Vector3 fp = new Vector3();
		Vector3 ep = new Vector3();
		List<Vector3> edges = new ArrayList<>();
		
		// Sum together all face points and make a list of
		// all edges containing the vertex v
		for(int i = 0; i < vTris.length; i ++) {
			fp.x += (meshVertices[vTris[i]].x + meshVertices[vTris[i]+1].x + meshVertices[vTris[i]+2].x) / 3;
			fp.y += (meshVertices[vTris[i]].y + meshVertices[vTris[i]+1].y + meshVertices[vTris[i]+2].y) / 3;
			fp.z += (meshVertices[vTris[i]].z + meshVertices[vTris[i]+1].z + meshVertices[vTris[i]+2].z) / 3;
			
			// Since this algorithm is going to be run on meshes composed of
			// triangles, every vertex that is on a face connected to v is
			// either v, or it forms an edge with v. If it is not v, we
			// add it to the edges list.
			
			if(meshVertices[vTris[i]] !=v && !edges.contains(meshVertices[vTris[i]])) {
				edges.add(meshVertices[vTris[i]]);
			}
			
			if(meshVertices[vTris[i]+1] !=v && !edges.contains(meshVertices[vTris[i]+1])) {
				edges.add(meshVertices[vTris[i]+1]);
			}
			
			if(meshVertices[vTris[i]+2] != v && !edges.contains(meshVertices[vTris[i]+2])) {
				edges.add(meshVertices[vTris[i]+2]);
			}
		}
		
		// At the moment the edges array only contains one vertex for
		// every edge: the one that v connects to. In order to find
		// the midpoints, we need to add that vertex to v and
		// divide by two. We can then sum that.
		for(int i = 0; i < edges.size(); i ++) {
			ep.x += (v.x + edges.get(i).x) / 2;
			ep.y += (v.y + edges.get(i).y) / 2;
			ep.z += (v.z + edges.get(i).z) / 2;
		}
		
		// Divide fp and ep by their respective number of elements.
		fp.x /= vTris.length;
		fp.y /= vTris.length;
		fp.z /= vTris.length;
		ep.x /= edges.size();
		ep.y /= edges.size();
		ep.z /= edges.size();
		
		// Barycenter = (F+2R+(n-3)P)/n
		// F: average of face points touching P
		// R: average of edge midpoints of edges touching P
		// n: the number of edge midpoints
		// P: the point in question
		// Source: Wikipedia (see top)
		Vector3 result = new Vector3();
		result.x = (fp.x + ep.x * 2 + v.x * (vTris.length - 3)) / vTris.length;
		result.y = (fp.y + ep.y * 2 + v.y * (vTris.length - 3)) / vTris.length;
		result.z = (fp.z + ep.z * 2 + v.z * (vTris.length - 3)) / vTris.length;
		return result;
	}
}