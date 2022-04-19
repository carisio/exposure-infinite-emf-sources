# Simulation files for the paper "Simultaneous Exposure to Infinite RF-EMF Sources from Typical Base Stations"

This repository contains files to reproduce the results of the paper "Simultaneous Exposure to Infinite RF-EMF Sources from Typical Base Stations" (accepted for publication in IEEE Antennas and Propagation Magazine) [DOI: 10.1109/MAP.2021.3106829](https://doi.org/10.1109/MAP.2021.3106829).

This repository has two folders: 'java code' and 'matlab code'.

## 'matlab code' folder

This folder contains the Matlab code used to generate the figures and tables of the paper. Among several files, it contains:

- eq4_finite_rings.m : Implementation of equation 4
- eq5_infinity_rings.m: Implementation of equation 5
- eq7_M_PL_models.m: Implementation of equation 7
- run_tests.m: Run tests of the above implementations.
- run_section_a.m: Shows the figures and results to generate the contents of the section III.A of the paper. The user can also use the file 'matlab code\spreedsheet validation\validation.xlsx' to compare the results.
- run_section_b.m: Shows the figures and resultss to generate the contents of the section III.B of the paper. Note that the input of the figures is a simulation implemented in Java, which is in the 'java code' folder.

## 'java code' folder

This folder provides a simulation to generate the results of section III.B of the paper. The results are used as input to 'run_section_b.m' to plot the results.

The files in this folder were forked from the repository https://github.com/carisio/emf-exposure/ . The original implementation appears in "The Design and Development of an App to Compute Exposure to Electromagnetic Fields [EM Programmer's Notebook]", published in IEEE Antennas and Propagation Magazine, Vol. 59, Issue 2, 2017 (DOI: 10.1109/MAP.2017.2655527).

To run the simulation, run the file 'src\telecom\tests'.
