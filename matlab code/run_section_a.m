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
nRings = 50;
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
TER_acc = sum(ER_acc, 1)';
TER_lim = ones(nRings, 1)*TER;
percentage_acc_to_lim = TER_acc/TER;

% Plot TER considering contributions of different rings
rings=1:nRings;
d = R(1)*rings;
figure(1);
fig1 = plot(rings, TER_acc, rings, TER_lim);
xlabel('n^{th} ring');
ylabel('Exposure ratio and percentage of the calculated TER');
set(fig1(1),'Color', 'black', 'LineWidth', 3, 'LineStyle', '.');
set(fig1(2),'Color', 'black', 'LineWidth', 3, 'LineStyle', '-');
legend('Contribution of the first n rings to the ER', 'TER limit');
legend('location', 'southeast');
axis([0 nRings 0 ceil(TER)]);
set(gca, 'XTick', 0:5:50)
% Change YTickLabel to show the percentage of the maximum TER
yticks = get(gca, 'YTick');
newLabel = '';
for i=1:length(yticks)
    if (yticks(i) < TER && yticks(i) ~= 0)
        newLabel = strcat(newLabel, sprintf('%1.1f (%1.0f %%)', yticks(i), 100*yticks(i)/TER));
    else
        newLabel = strcat(newLabel, sprintf('%1.1f', yticks(i)));
    end
    if (i < length(yticks))
        newLabel = strcat(newLabel, '|');
    end
end
set(gca, 'YTickLabel', newLabel);
grid;

% Plot measurements.
% Column 1: Latitude
% Column 2: Longitude
% Column 3: electric field (V/m)
measurements = load('measurements brasilia/measurements.txt');
s_measured = measurements(:,3).^2/377;
s_measured = sort(s_measured);
samples = length(s_measured);
s_max_calculated = sum(S);

figure(2);
% fig2 = cdfplot(s_measured);
fig2 = semilogx(s_measured, 1/samples:1/samples:1);
%title('cdf of the power density measured in Brasília');
xlabel('x');
ylabel('F(x)');
set(fig2,'Color', 'black', 'LineWidth', 2, 'LineStyle', '.');
% xticks = 0:s_max_calculated/5:s_max_calculated; For linear scale
% xticks = [10e-5 10e-4 10e-3 10e-2 10e-1 10e0];
xticks = [s_max_calculated/10000 s_max_calculated/1000 s_max_calculated/100 s_max_calculated/10 s_max_calculated];
newLabel = '';
for i=1:length(xticks)
    newLabel = strcat(newLabel, sprintf('%1.0e (%1.1f %%)', xticks(i), 100*xticks(i)/s_max_calculated));
    if (i < length(xticks))
        newLabel = strcat(newLabel, '|');
    end
    
end
set(gca, 'XTick', xticks);
set(gca, 'XTickLabel', newLabel);
axis([0 s_max_calculated 0 1])
grid

% Plot EMF Estimator data
% Column 1: Distance X [m]
% Total exposure [%]
% LTE2600 [%]
% UMTS2100 [%]
% LTE1800 [%]
% UMTS850 [%]
% LTE700 [%]
emfestimatordata = load('emf estimator project/emfestimatordata.csv');
figure(3);
fig3 = plot(emfestimatordata(:,1), emfestimatordata(:,2:7));
set(fig3(1),'Color', 'black', 'LineWidth', 3, 'LineStyle', '-');
set(fig3(2),'Color', 'black', 'LineWidth', 3, 'LineStyle', '--'); % OK
set(fig3(3),'Color', [0.5 0.5 0.5], 'LineWidth', 3, 'LineStyle', '--'); % OK
set(fig3(4),'Color', [0.5 0.5 0.5], 'LineWidth', 3, 'LineStyle', '-'); 
set(fig3(5),'Color', 'black', 'LineWidth', 3, 'LineStyle', ':'); % OK
set(fig3(6),'Color', [0.5 0.5 0.5], 'LineWidth', 3, 'LineStyle', ':'); % OK
legend('TER', 'LTE 2600 MHz', 'UMTS 2100 MHz', 'LTE 1800 MHz', 'UMTS 850 MHz', 'LTE 700 MHz');
xlabel('Distance from base station [m]');
ylabel('Exposure ratio [%]');
grid;