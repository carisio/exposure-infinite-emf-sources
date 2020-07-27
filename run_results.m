close all;
clear all;
clc;

disp('--------------------------------------------------------------------------------------');
disp('Results');
fprintf('\n');

% Lower frequencies at each band
f700 = 700e6;
f800 = 800e6;
f1800 = 1800e6;
f2100 = 2100e6;
f2600 = 2600e6;

% Lambdas
lamb700 = 3e8/f700;
lamb800 = 3e8/f800;
lamb1800 = 3e8/f1800;
lamb2100 = 3e8/f2100;
lamb2600 = 3e8/f2600;

% Height of the base stations and the probe
hbs700 = 30;
hbs800 = 30;
hbs1800 = 25;
hbs2100 = 20;
hbs2600 = 20;
hprobe = 1.5;

% Propagation models for all frequencies
gamma = [2, 4];

C700 = [(4*pi/lamb700)^2 1/(hbs700*hprobe)^2];
C800 = [(4*pi/lamb800)^2 1/(hbs800*hprobe)^2];
C1800 = [(4*pi/lamb1800)^2 1/(hbs1800*hprobe)^2];
C2100 = [(4*pi/lamb2100)^2 1/(hbs2100*hprobe)^2];
C2600 = [(4*pi/lamb2600)^2 1/(hbs2600*hprobe)^2];

% Radius for all frequencies
R700 = 123;
R800 = 123;
R1800 = 123;
R2100 = 123;
R2600 = 123;

% Transition between PL1 and PL2 is at d = 1000 m
d700=[4*pi*hbs700*hprobe/lamb700];
d800=[4*pi*hbs800*hprobe/lamb800];
d1800=[4*pi*hbs1800*hprobe/lamb1800];
d2100=[4*pi*hbs2100*hprobe/lamb2100];
d2600=[4*pi*hbs2600*hprobe/lamb2600];

% EIRP for all base stations is 63 dBm ~ 2.000 W
EIRP=1000;

% Safety limit for S at each frequency
S_lim_700 = 3.5;
S_lim_800 = 4;
S_lim_1800 = 9;
S_lim_2100 = 10;
S_lim_2600 = 10;

% Calculate the contribution for all frequencies
S_700 = eq7_M_PL_models(C700, gamma, d700, R700, EIRP, f700);
S_800 = eq7_M_PL_models(C800, gamma, d800, R800, EIRP, f800);
S_1800 = eq7_M_PL_models(C1800, gamma, d1800, R1800, EIRP, f1800);
S_2100 = eq7_M_PL_models(C2100, gamma, d2100, R2100, EIRP, f2100);
S_2600 = eq7_M_PL_models(C2600, gamma, d2600, R2600, EIRP, f2600);

% Calculate ER for each frequency
ER_700 = 100*S_700/S_lim_700;
ER_800 = 100*S_800/S_lim_800;
ER_1800 = 100*S_1800/S_lim_1800;
ER_2100 = 100*S_2100/S_lim_2100;
ER_2600 = 100*S_2600/S_lim_2600;

% Calculate S and TER considering all base stations
S = S_700 + S_800 + S_1800 + S_2100 + S_2600;
TER = ER_700 + ER_800 + ER_1800 + ER_2100 + ER_2600;

fprintf('Frequency\tS\t\t\tS_lim\t\tER\n');
fprintf('%2f\t%2f\t%2f\t%2f\n', 700, S_700, S_lim_700, ER_700);
fprintf('%2f\t%2f\t%2f\t%2f\n', 800, S_800, S_lim_800, ER_800);
fprintf('%2f\t%2f\t%2f\t%2f\n', 1800, S_1800, S_lim_1800, ER_1800);
fprintf('%2f\t%2f\t%2f\t%2f\n', 2100, S_2100, S_lim_2100, ER_2100);
fprintf('%2f\t%2f\t%2f\t%2f\n', 2600, S_2600, S_lim_2600, ER_2600);

fprintf('\nConsidering all base stations\n');
fprintf('S\t\t\tTER\n');
fprintf('%2f\t%2f\n', S, TER);
