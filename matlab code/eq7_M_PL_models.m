% Using equation 4 for n = i...j
% Path loss model: C*d^gamma
%                  C and gamma are arrays of size M
% dPL: array of size M - 1. 
%      PL1 is applied for distances between [0, dPL[1]]
%      PL2 is applied for distances between [dPL[1], dPL[2]]
%      ...
%      PLM-1 is applied for distances between [dPL[M-2], dPL[M-1]]
%      PLM is applied for distances > dPL[M]
% R: radius of the inner circular ring (m)
% EIRP: effective isotropic radiation power of the base stations (W)
% f: frequency of the radio source (Hz)
% n: indexes of the rings to return Sn

function S = eq7_M_PL_models(C, gamma, dPL, R, EIRP, f)
M = length(C);

lambda = 3e8/f;

mult = 4*pi*EIRP/lambda^2;
I = floor(dPL/R);

S = 0;
n = 0;
for m=1:M-1
    if m == 1
        n = 1:I(m);
    else
        n=(I(m-1)+1):I(m);
    end
    [temp, Sm] = eq4_finite_rings(C(m), gamma(m), R, EIRP, f, n);
    S = S + Sm;
end

[temp, Smminusone] = eq4_finite_rings(C(M), gamma(M), R, EIRP, f, 1:I(M-1));
S = S + eq5_infinity_rings(C(M), gamma(M), R, EIRP, f) - Smminusone;