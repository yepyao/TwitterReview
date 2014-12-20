function [ data ] = read_cell( file, dim )
%READ_CELL Summary of this function goes here
%   Detailed explanation goes here
data = cell(dim,1,0);
fin = fopen('hmm_o.txt');
count = 0;
while ~feof(fin)
    line = fgetl(fin);
    a = sscanf(line,'[ %f %f %f]; ');
    l = size(a,1)/3;
    count = count + 1;
    for i=1:l
        data{1,1,count}(i) = a((i-1)*3+1);
        data{2,1,count}(i) = a((i-1)*3+2);
        data{3,1,count}(i) = a((i-1)*3+3);
    end
end

end

