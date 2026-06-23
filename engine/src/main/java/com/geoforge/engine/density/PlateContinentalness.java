package com.geoforge.engine.density;

import com.geoforge.engine.geology.TectonicPlateMapper;

/**
 * A density function that maps tectonic plate continentalness to a height offset.
 *
 * <p>Uses a {@link TectonicPlateMapper} to compute continentalness at each (x, z) position,
 * scaled by the configured amplitude and shifted by a base offset. The Y coordinate is
 * ignored because continentalness is a 2D property.
 *
 * @param mapper                    the tectonic plate mapper
 * @param continentalBase           minimum height contribution (used for ocean basins)
 * @param continentalHeightAmplitude height range added to the base by continental crust
 */
public record PlateContinentalness(
        TectonicPlateMapper mapper,
        double continentalBase,
        double continentalHeightAmplitude)
        implements DensityFunctionTree {

    @Override
    public double sample(double x, double y, double z) {
        float c = mapper.getContinentalness((int) x, (int) z);
        return continentalBase + c * continentalHeightAmplitude;
    }
}
