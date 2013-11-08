package ch.fhnw.pfcs.satellites.objects;

public class GeostationarySatellite extends Satellite {

	private static final double GEOSTATIONARY_RADIUS = 42050;
	
	public GeostationarySatellite(Planet center, double initialRotationPos, double secsPerDraw) {
		super(center, GEOSTATIONARY_RADIUS, ONE_DAY, 0, initialRotationPos, secsPerDraw);
	}
	
	@Override
	protected int[] getOrbitColor() {
		return new int[]{255,255,0};
	}
}
