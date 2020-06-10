yields = importyields();

sample = timerange('1992-03-01', '1999-12-01', 'months');
% sample = timerange('1992-03-01', '1999-12-01', 'months');
% sample = timerange('1992-03-01', '1999-12-01', 'months');
% sample = timerange('1992-03-01', '1999-12-01', 'months');

maturities = [12 24 36 48 60 72 84 96 108 120]';

lambda0 = 0.0609;
X = [ones(size(maturities)) (1-exp(-lambda0*maturities))./(lambda0*maturities) ...
    ((1-exp(-lambda0*maturities))./(lambda0*maturities)-exp(-lambda0*maturities))];
y = yields(sample, maturities / 12);

forecastmats = [12 24 36 60 120]';
yy = yields(sample, forecastmats / 12);

estimates = zeros(size(yy,1), size(yy,2), 2);

for i = 1:size(y, 1)
    training = timerange(datestr(addtodate(datenum(y.date(1)), -120, 'month')), ...
        datestr(addtodate(datenum(y.date(1)), -1, 'month')), 'months');
    ty = yields(training, maturities / 12);
    beta = zeros(size(ty, 1), 3);

    for j = 1:size(ty, 1)
        EstMdlOLS = fitlm(X, ty(j, :).Variables', 'Intercept', false);
        beta(j,:) = EstMdlOLS.Coefficients.Estimate';
    end

    EstMdlAR_1 = estimate(arima('Constant',NaN,'ARLags',1,'Distribution','Gaussian'),beta(:,1),'Display','off');
    EstMdlAR_2 = estimate(arima('Constant',NaN,'ARLags',1,'Distribution','Gaussian'),beta(:,2),'Display','off');
    EstMdlAR_3 = estimate(arima('Constant',NaN,'ARLags',1,'Distribution','Gaussian'),beta(:,3),'Display','off');
    EstMdlVAR = estimate(varm(3,1),beta,'Display','off');
    
    fbeta1 = forecast(EstMdlAR_1, 1, beta(:,1));
    fbeta2 = forecast(EstMdlAR_2, 1, beta(:,2));
    fbeta3 = forecast(EstMdlAR_3, 1, beta(:,3));
    fbeta = forecast(EstMdlVAR, 1, beta);
    
    XX = [ones(size(forecastmats)) (1-exp(-lambda0*forecastmats))./(lambda0*forecastmats) ...
        ((1-exp(-lambda0*forecastmats))./(lambda0*forecastmats)-exp(-lambda0*forecastmats))];
    
    fyar = XX * [fbeta1 fbeta2 fbeta3]';
    fyvar = XX * fbeta';
    
    estimates(i,:,1) = fyar';
    estimates(i,:,2) = fyvar';
end

spe = (estimates - yy.Variables).^2;
mspe = mean(spe, 1);