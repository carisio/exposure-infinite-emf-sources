close all;
clear all;
clc;

disp('--------------------------------------------------------------------------------------');
disp('Results');
fprintf('\n');

% Lower frequencies at each band and wavelength
freq = [700e6; 850e6; 1800e6; 2100e6; 2600e6];
lambda = 3e8./freq;
% Base station height (hbs) and probe height (hprobe)
hbs = [30; 30; 25; 20; 20];
hprobe = 1.5;

% Propagation models for all frequencies
gamma = [2, 4];
C = [(4*pi./lambda).^2    1./(hbs*hprobe).^2];

% Radius for all frequencies
R = 123*ones(size(freq));

% Transition between PL1 and PL2 is at d = 1000 m
d_transition = 4*pi*hbs*hprobe./lambda;

% EIRP for all base stations is 63 dBm ~ 2.000 W
EIRP=1000*ones(size(freq));

% Safety limit for S at each frequency
S_lim = [3.5; 4.25; 9; 10; 10];

% Calculate the contribution for all frequencies
S = zeros(size(freq));
ER = zeros(size(freq));
TER = 0;
for i=1:length(freq)
    S(i) = eq7_M_PL_models(C(i,:), gamma, d_transition(i), R(i), EIRP(i), freq(i));
end
ER = 100*S./S_lim;
TER = sum(ER);

% Print results
fprintf('Freq\tS\t\t\tS_lim\t\tER\n');
for i=1:length(freq)
    freqMHz = freq(i)/1e6;
    if (freqMHz < 1000)
        fprintf('%d\t\t%2f\t%2f\t%2f\n', freq(i)/1e6, S(i), S_lim(i), ER(i));
    else
        fprintf('%d\t%2f\t%2f\t%2f\n', freq(i)/1e6, S(i), S_lim(i), ER(i));
    end
end
fprintf('\nConsidering all base stations\n');
fprintf('S\t\t\tTER\n');
fprintf('%2f\t%2f\n', sum(S), TER);

% Let's plot the accumulated TER considering the influence ring a ring
% we are considering that the inner ring is the same regardless the
% frequency
% PS.: The calculation below assumes that the inner ring is the same
% for all frequencies. 
nRings = 82;
TER_acc = zeros(nRings, 1);
nFreq = length(freq);
S_acc = zeros(nFreq, nRings);
ER_acc = zeros(nFreq, nRings);
for f=1:nFreq
    % We are using two propagation models. It is necessary two calculate
    % the contribution of both and select the one that apply
    S_pl_model_1 = eq4_finite_rings(C(f,1), gamma(1), R(f), EIRP(f), freq(f), 1:nRings);
    S_pl_model_2 = eq4_finite_rings(C(f,2), gamma(2), R(f), EIRP(f), freq(f), 1:nRings);
    use_PL_1 = (1:nRings)* R(f) < d_transition(f);
    S_at_ring = (use_PL_1 .* S_pl_model_1) + (1 - use_PL_1).*S_pl_model_2;
    for r=1:nRings
        S_acc(f,r) = sum(S_at_ring(1:r));
    end
    ER_acc(f, :) = 100 * S_acc(f, :) / S_lim(f);
end
TER_acc = sum(ER_acc, 1);
TER_lim = ones(nRings)*TER;
percentage_acc_to_lim = TER_acc/TER;
d = R(1)*(1:nRings);
figure(1);
plot(d, TER_acc, d, TER_lim);
figure(2);
plot(d, percentage_acc_to_lim);