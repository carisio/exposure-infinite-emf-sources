close all;
clear all;
clc;

disp('--------------------------------------------------------------------------------------');
disp('Results');
fprintf('\n');

% Load ERBs and NIR data and convert the data of electric fiel to power
% density
erbs700 = dlmread('monte carlo section b data/bs 700MHz.txt', '\t', 3, 0);
nir700 = dlmread('monte carlo section b data/nir 700MHz.txt', '\t', 3, 0);
s700 = nir700(:, 3).^2./377;

erbs850 = dlmread('monte carlo section b data/bs 850MHz.txt', '\t', 3, 0);
nir850 = dlmread('monte carlo section b data/nir 850MHz.txt', '\t', 3, 0);
s850 = nir850(:, 3).^2./377;

erbs1800 = dlmread('monte carlo section b data/bs 1800MHz.txt', '\t', 3, 0);
nir1800 = dlmread('monte carlo section b data/nir 1800MHz.txt', '\t', 3, 0);
s1800 = nir1800(:, 3).^2./377;

erbs2100 = dlmread('monte carlo section b data/bs 2100MHz.txt', '\t', 3, 0);
nir2100 = dlmread('monte carlo section b data/nir 2100MHz.txt', '\t', 3, 0);
s2100 = nir2100(:, 3).^2./377;

erbs2600 = dlmread('monte carlo section b data/bs 2600MHz.txt', '\t', 3, 0);
nir2600 = dlmread('monte carlo section b data/nir 2600MHz.txt', '\t', 3, 0);
s2600 = nir2600(:, 3).^2./377;

s_sim_tot = s700 + s850 + s1800 + s2100 + s2600;

% Plot erbs and probes positions
figure(1);
fig1 = plot(erbs700(1:500,1)/1000, erbs700(1:500,2)/1000, 'o', nir700(1:1000,1)/1000, nir700(1:1000,2)/1000, '.');
xlabel('x [km]');
ylabel('y [km]');
set(fig1(2),'Color', 'black', 'LineWidth', 2);
set(fig1(1),'Color', [0.5 0.5 0.5], 'LineWidth', 2);
legend('Base station', 'Probe');

% Calculate the maximum simulated s for each frequency
S_sim_max = [max(s700) max(s850) max(s1800) max(s2100) max(s2600)];

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% BEGIN: S calculated acording to eq. 7
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
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
S_eq_7 = zeros(size(freq));
s_sim_tot_eq_7 = 0;
s_calc_tot = 0;
for i=1:length(freq)
    S_eq_7(i) = eq7_M_PL_models(C(i,:), gamma, d_transition(i), R(i), EIRP(i), freq(i));
    s_sim_tot_eq_7 = s_sim_tot_eq_7 + S_eq_7(i);
    s_calc_tot = s_calc_tot + max(S_eq_7(i));
end
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% END: S calculated acording to eq. 7
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Compare the maximum simulated with the limit given by equation 7
% Print results
fprintf('Freq\tS_sim_max\t\tS_eq_7\n');
for i=1:length(freq)
    freqMHz = freq(i)/1e6;
    if (freqMHz < 1000)
        fprintf('%d\t\t%2f\t%2f\n', freq(i)/1e6, S_sim_max(i), S_eq_7(i));
    else
        fprintf('%d\t%2f\t%2f\n', freq(i)/1e6, S_sim_max(i), S_eq_7(i));
    end
end

samples = length(s_sim_tot);
s_sim_tot = sort(s_sim_tot);
figure(3);
fig3 = plot(s_sim_tot, 1/samples:1/samples:1);
xlabel('x');
ylabel('F(x)');
set(fig3,'Color', 'black', 'LineWidth', 2, 'LineStyle', '.');
xticks = [0.25*s_calc_tot 0.5*s_calc_tot 0.75*s_calc_tot s_calc_tot];
newLabel = '';
for i=1:length(xticks)
    newLabel = strcat(newLabel, sprintf('%1.0e (%1.1f %%)', xticks(i), 100*xticks(i)/s_calc_tot));
    if (i < length(xticks))
        newLabel = strcat(newLabel, '|');
    end    
end
set(gca, 'XTick', xticks);
set(gca, 'XTickLabel', newLabel);
axis([0 s_calc_tot 0 1])
grid

