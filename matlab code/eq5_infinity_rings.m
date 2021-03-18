% Using equation 4 for n = i...j
% Path loss model: C*d^gamma
% R: radius of the inner circular ring (m)
% EIRP: effective isotropic radiation power of the base stations (W)
% f: frequency of the radio source (Hz)

function S = eq5_infinity_rings(C, gamma, R, EIRP, f)
lambda = 3e8/f;
S = (EIRP*4*pi / (C * R^gamma * lambda^2)) *( 2 * zeta(gamma - 1) - zeta(gamma) );
