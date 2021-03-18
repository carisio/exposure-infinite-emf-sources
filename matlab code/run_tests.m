close all;
clear all;
clc;

% Test case 1
disp('Test case 1:');
disp('Using eq. 4 to find S in the first 10 rings using FSPL model');
disp('PL = 1757.923614*d^2');
fprintf('\n');

C=1757.923614;
gamma=2;
R=100;
f=1e9;
EIRP=1000;
n=1:10;
expected = 0.034218429;
[Sn, S] = eq4_finite_rings(C, gamma, R, EIRP, f, n);
disp(sprintf('eq4: %d', S));
disp(sprintf('Expected: %d', expected));
disp(sprintf('Error: %d%%', 100*(S-expected)/expected)); 

fprintf('\n');
disp('--------------------------------------------------------------------------------------');
disp('Test case 2:');
disp('Using eq5 to find S in infinity rings');
disp('The result are compared with the one given by eq. 4 for the first 10000 rings');
disp('The path loss model is the same as FSPL but with a different path loss exponent');
disp('For rapid convergence of eq. 4, gamma will be set to 2.7');
disp('PL = 1757.923614*d^2.7');
fprintf('\n');

C=1757.923614;
gamma=2.7;
R=100;
f=1e9;
EIRP=1000;
n=1:10000;
[Sn, S_eq4] = eq4_finite_rings(C, gamma, R, EIRP, f, n);
S_eq5 = eq5_infinity_rings(C, gamma, R, EIRP, f);
disp(sprintf('eq4: %d', S_eq4));
disp(sprintf('eq5: %d', S_eq5));
disp(sprintf('Error (difference from eq4): %d%%', 100*(S_eq5-S_eq4)/S_eq4));

fprintf('\n');
disp('--------------------------------------------------------------------------------------');
disp('Test case 3:');
disp('Using different path loss models to describe the environment');
disp('In this test, we will use eq7_M_PL_models to find S with different');
disp('path loss model. But the same path loss model will be used in all cases,');
disp('which implies that the result should be the same as the one given by eq5.');
disp('The path loss model is the same as FSPL but with a different path loss exponent:');
disp('PL = 1757.923614*d^2.7');
fprintf('\n');

C=[1757.923614, 1757.923614, 1757.923614, 1757.923614];
gamma=[2.7, 2.7, 2.7, 2.7];
dPL=[150, 300, 450];
R=100;
f=1e9;
EIRP=1000;
S_eq7 = eq7_M_PL_models(C, gamma, dPL, R, EIRP, f);
S_eq5 = eq5_infinity_rings(C(1), gamma(1), R, EIRP, f);
disp(sprintf('eq5: %d', S_eq5));
disp(sprintf('eq7: %d', S_eq7));
disp(sprintf('Error (difference from eq5): %d%%', 100*(S_eq7-S_eq5)/S_eq5));

fprintf('\n');
disp('--------------------------------------------------------------------------------------');
disp('Test case 4:');
disp('Using 2 different path loss models to describe the environment.');
disp('In the first 1 km the PL model is the FSPL.');
disp('For d > 1000 km, the path loss exponent is changed to 2.7.');
disp('To ensure continuity of the PL model, the value of C for PL2 is:');
disp('C2 = C1 * 1000^(gama1 - gama2)');
fprintf('\n');
C=[1757.923614, 13.96368361];
gamma=[2, 2.7];
dPL=[1000];
R=100;
f=1e9;
EIRP=1000;
[temp, S_eq4_first_10_rings] = eq4_finite_rings(C(1), gamma(1), R, EIRP, f, 1:10);
[temp, S_eq4_rings_11_to_1000] = eq4_finite_rings(C(2), gamma(2), R, EIRP, f, 11:1000);
approximation = S_eq4_first_10_rings + S_eq4_rings_11_to_1000;
S_eq7 = eq7_M_PL_models(C, gamma, dPL, R, EIRP, f);
disp(sprintf('eq7: %d', S_eq7));
disp(sprintf('First 1000 rings: %d', approximation));
disp(sprintf('Error: %d%%', 100*(S_eq7-approximation)/approximation)); 