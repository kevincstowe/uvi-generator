
% Visual Preposition Class Hierarchy
% ----------------------------------
%   spatial
%    |---> loc
%    |---> path
%           |---> dir
%           |---> src
%           |---> dest
%                  |---> dest_conf
%                  |---> dest_dir

% dynamic spatial prep
isa(src,path)
isa(dir,path)
isa(dest,path)
isa(dest_conf,dest)
isa(dest_dir,dest)
isa(path,spatial)
isa(loc,spatial)

% src
isa(from,src)
isa(out,src)
isa(out_of,src)
isa(off,src)
isa(off_of,src)

% dest_conf
isa(into,dest_conf)
isa(onto,dest_conf)

% dest_dir
isa(for,dest_dir) %only for intrans?
isa(at,dest_dir)
isa(to,dest_dir)
isa(towards,dest_dir)

% dir
isa(across,dir)
isa(along,dir)
isa(around,dir)
isa(down,dir)
isa(over,dir)
isa(past,dir)
isa(round,dir)
isa(through,dir)
isa(towards,dir)
isa(up,dir)

% loc
isa(about,loc)
isa(above,loc)
isa(against,loc)
isa(along,loc)
isa(alongside,loc)
isa(amid,loc)
isa(among,loc)
isa(amongst,loc)
isa(around,loc)
isa(astride,loc)
isa(at,loc)
isa(athwart,loc)
isa(before,loc)
isa(behind,loc)
isa(below,loc)
isa(beneath,loc)
isa(beside,loc)
isa(between,loc)
isa(beyond,loc)
isa(by,loc)
(?)isa(from,loc)
isa(in,loc)
isa(in_front_of,loc)
isa(inside,loc)
isa(near,loc)
isa(next_to,loc)
isa(off,loc)
isa(on,loc)
isa(opposite,loc)
isa(out_of,loc)
isa(outside,loc)
isa(over,loc)
isa(round,loc)
isa(throughout,loc)
isa(under,loc)
isa(underneath,loc)
isa(upon,loc)
isa(within,loc)

