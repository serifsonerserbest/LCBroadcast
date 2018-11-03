files = ['da_proc_1.out', 'da_proc_2.out', 'da_proc_3.out']



for f in files:
    p_1, p_2, p_3 = [], [], []
    FILE = open(f, 'r')
    lines = FILE.readlines()
    for line in lines:
        out = line.split(' ')
        p_id = int(out[0])
        m_id = int(out[1])
        if p_id == 1:
           p_1.append(m_id)
        elif p_id == 2:
           p_2.append(m_id)
        else:
           p_3.append(m_id)
    print(p_1, p_2, p_3)
    if len(p_1) == len(set(p_1)):
        print('p_1 length is okay')
    if len(p_2) == len(set(p_2)):
        print('p_2 length is okay')
    if len(p_2) == len(set(p_3)):
        print('p_3 length is okay')
    if p_1 == sorted(p_1):
        print('p_1 seq is okay')
    if p_2 == sorted(p_2):
        print('p_2 seq is okay')
    if p_3 == sorted(p_3):
        print('p_3 seq is okay')
    
