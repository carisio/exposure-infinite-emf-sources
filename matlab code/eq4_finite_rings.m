% Using equation 4 for n = i...j
% Path loss model: C*d^gamma
% R: radius of the inner circular ring (m)
% EIRP: effective isotropic radiation power of the base stations (W)
% f: frequency of the radio source (Hz)
% n: indexes of the rings (vector)

function [Sn, S] = eq4_finite_rings(C, gamma, R, EIRP, f, n)
lambda = 3e8/f;
Sn = (EIRP*4*pi / (C * R^gamma * lambda^2)) *( (2*n - 1)./(n.^gamma) );
S = sum(Sn);