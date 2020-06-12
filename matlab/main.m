yields = import_yields();
macro = import_macro();

yieldsNorm = normalize(yields.Variables);
macroNorm = normalize(macro.Variables);
pcaYieldsCoeff = pca(yieldsNorm);
pcaMacroCoeff = pca(macroNorm);
spcaYieldsCoeff = spca(yieldsNorm, [], 3, inf, -[5, 3, 2]);
spcaMacroCoeff = spca(macroNorm, [], 3, inf, -[75, 50, 25]);

pcaYields = array2timetable(yieldsNorm*pcaYieldsCoeff, 'RowTimes', yields.date);
pcaMacro = array2timetable(macroNorm*pcaMacroCoeff, 'RowTimes', macro.date);
spcaYields = array2timetable(yieldsNorm*spcaYieldsCoeff, 'RowTimes', yields.date);
spcaMacro = array2timetable(macroNorm*spcaMacroCoeff, 'RowTimes', macro.date);

% sample = timerange('1992-01-01', '1999-12-01', 'months');
sample = timerange('2000-01-01', '2007-12-01', 'months');
% sample = timerange('2008-01-01', '2016-12-01', 'months');
% sample = timerange('1992-01-01', '1999-12-01', 'months');

cols = [1, 2, 3, 5, 10]; % tau = 1, 2, 3, 5, 10
y = yields(sample, cols);

nModels = 14;
forecasts = zeros(size(y, 1), size(y, 2), nModels);

ticBytes(gcp);
parfor i=1:size(y,1)
    trainingSample = timerange(datestr(addtodate(datenum(y.date(i)), -120, 'month')), ...
        datestr(addtodate(datenum(y.date(i)), -1, 'month')), 'months'); % rolling window of 120 months
    % hack to fit VARX(.) models with lagged regressors
    factorSample = timerange(datestr(addtodate(datenum(y.date(i)), -121, 'month')), ...
        datestr(addtodate(datenum(y.date(i)), -1, 'month')), 'months'); % rolling window of 120 months
    
    trainingFullYields = yields(trainingSample, :).Variables;
    trainingYields = yields(trainingSample, cols).Variables;
    traningPcaMacro = pcaMacro(factorSample, :).Variables;
    
    f = zeros(size(y, 2), nModels);
    f(:, 1) = ar_1(trainingYields);
    f(:, 2) = var_1(trainingYields);
    f(:, 3) = var_1_factors(trainingYields, traningPcaMacro(:, 1));
    f(:, 4) = var_1_factors(trainingYields, traningPcaMacro(:, 1:2));
    f(:, 5) = ar_sic(trainingYields);
    f(:, 6) = var_sic(trainingYields);
    f(:, 7) = var_sic_factors(trainingYields, traningPcaMacro(:, 1));
    f(:, 8) = var_sic_factors(trainingYields, traningPcaMacro(:, 1:2));
    f(:, 9) = dns_ar(trainingFullYields, [12 24 36 48 60 72 84 96 108 120]);
    f(:, 10) = dns_ar(trainingFullYields, [12 24 36 60 84 120]);
    f(:, 11) = dns_ar(trainingFullYields, [12 36 60 120]);
    f(:, 12) = dns_var(trainingFullYields, [12 24 36 48 60 72 84 96 108 120]);
    f(:, 13) = dns_var(trainingFullYields, [12 24 36 60 84 120]);
    f(:, 14) = dns_var(trainingFullYields, [12 36 60 120]);
    
    forecasts(i, :, :) = f;
end
tocBytes(gcp)

spe = (forecasts - y.Variables).^2;
mspe = squeeze(mean(spe, 1))';
rmspe = mspe ./ mspe(1, :);