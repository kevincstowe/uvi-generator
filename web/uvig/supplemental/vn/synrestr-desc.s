File Format is as follows:
   Number: COMLEX class name
   Example
   VerbNet surface syntax

The COMLEX class names can be found in the "COMLEX Syntax Reference Manual:
Version 3.0" Explanations of the COMLEX feature values may be found in the
"COMLEX Word Classes Manual" (1998). Both may be downlaoded from
http://nlp.cs.nyu.edu/comlex/index.html.

Update: 6/17/04: Finished work on VP-complements.

*****************************************************************
/////////////////////////////////////////////////////////////////
\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
/////////////////////////////////////////////////////////////////
\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
******************************************************************
# - doesn't occur in either new alternations or classes


#1. ADJP
The price sank low.
Attribute V Adj


#2. ADJP-PRED-RS


#3. ADVP
He meant well.

#4. ADVP-PRED-RS
He seems well.


5. AS-NP
"I worked as an apprentice cook."
Agent V as Predicate[-sentential]


6. EXTRAP-NP-S
"It annoys them that they left."
it V Experiencer Cause[+that_comp]

7. S-SUBJ-NP-OBJ
"That she left annoys them."
Cause[+that_comp] V Experiencer.


8.  TO-INF-SUBJ-NP-OBJ
"To read pleases them."
Cause[+to_inf] V Experiencer


9. EXTRAP-TO-INF
"It remains to find a cure."
it V Cause[+to_inf]

10. EXTRAP-FOR-TO-INF
"It remains for us to find a cure."
it V for Theme Predicate[+to_inf -control_to_inf -rs_to_inf]

11. EXTRAP-NP-TO-INF
"It pleases them to find a cure."
it V Theme Predicate[+to_inf]

12. EXTRAP-TO-NP
"It matters to them that she left."
it V Prep(to) Experiencer Cause[+that_comp]

13. EXTRAP-TO-NP-TO-INF
"It occurred to them to watch."
it V Prep(to) Experiencer Predicate[+to_inf]

14. S-SUBJ-TO-NP
"That she left matters to them."
Cause[+that_comp] V Prep(to) Experiencer


15. FOR-TO-INF
"I prefer for her to do well."
Agent V Prep(for) Theme Predicate[+to_inf -control_to_inf -rs_to_inf]


16. HOW-S
"He asked how she did it."
Agent V Topic[+how_extract]


17. HOW-TO-INF
"He explained how to do it."
Agent V Topic[+wh_inf]


18. INF-AC
"He helped bake the cake."
Agent V Predicate[+ac_bare_inf]


19. ING-NP-OMIT
"His hair needs combing."
Patient V Predicate[+np_omit_ing]


20. ING-SC/BE-ING-SC
"She stopped smoking."
Agent V Predicate[+be_sc_ing]

21. ING-AC
"She discussed writing novels."
Agent V Topic[+ac_ing]

22. INTRANS
"He went."
Agent[-plural] V

23. INTRANS-RECIP(SUBJ-PL/COORD)
"They went."
Actor[+plural] V

24. NP
"He loved her."
Experiencer V Cause

25. NP-ADJP
"He painted the car black."
Agent V Theme Adj

26. NP-ADJP-PRED
"She considered him foolish."
Experiencer V Theme Adj

27. NP-ADVP
"He put it there."
Agent V Theme Destination[+adv_loc]

28. NP-AVDP-PRED
"They mistakenly thought him here."
Agent V Theme Location[+adv_loc]

29. NP-AS-NP
"I sent him as a messenger."
Agent V Theme as Predicate[-sentential]

30. NP-AS-NP-SC
"She served the firm as a researcher."
Agent V Theme as Predicate[-sentential]

31. NP-FOR-NP
"She bought a book for him."
Agent V Theme Prep(for) Recipient

32. NP-INF
"He made her sing."
Agent V Patient Predicate[+bare_inf]

33. NP-INF-OC
"He helped her bake the cake."
Agent V Beneficiary Predicate[+oc_bare_inf]

34. NP-ING
"I kept them laughing."
Agent V Patient Predicate[+acc_ing]

35. NP-ING-OC
"I caught him stealing."
Agent V Theme Predicate[+oc_ing]

36. NP-ING-SC
"He combed the woods looking for her."
Agent V Location Predicate[+sc_ing]


37. NP-NP
"She asked John a question."
Agent V Theme Topic[-sentential]


38. NP-NP-PRED
"They appointed him professor."
Agent V Theme Predicate[-sentential]


*39. NP-P-ING
"I prevented her from leaving."
Agent V Theme Prep() Predicate[+gerund -control_ing -np_omit_ing -poss_ing -acc_ing]
*In this case, content of Prep() filled with 'from'


40. NP-P-ING-OC
"I accused her of murdering her husband."
Agent V Maleficiary Prep() Predicate[+oc_ing]
*In this case, content of Prep() filled with 'of'


41. NP-P-ING-SC
"He wasted time on fussing with his hair."
Agent V Theme Prep() Predicate[+sc_ing]
*In this case, content of Prep() filled with 'on'


42. NP-P-ING-AC
"He told her about climbing the mountain."
Agent V Recipient Prep() Predicate[+ac_ing]


43. NP-P-NP-ING
"He attributed his failure to noone buying his books."
Agent V Theme Prep() Cause[+gerund -control_ing -np_omit_ing -poss_ing -acc_ing]
*In this case, the content of Prep() is 'to'


44. NP-P-POSSING
"They asked him about his participating in that conference."
Actor1 V Actor2 Prep() Topic[+poss_ing]
*In this case, content of Prep() is 'about'

45. NP-P-WH-S
"They made a great fuss about whether they should participate."
Agent V Oblique[-sentential] Prep() Topic[+wheth_comp]]
*In this case, Prep() is 'about'

46. NP-P-WHAT-S
"They made a great fuss about what they should do."
Agent V Oblique[-sentential] Prep() Topic[+what_extract]
*In this case, Prep() is 'about'
*Hardly see how COMLEX justifies classifying 'what' as a complementizer of the
same status as 'if' or 'whether'

47. NP-P-WHAT-TO-INF
"They made a great fuss about what to do."
Agent V Oblique Prep() Topic[+what_inf]
*In this case, Prep() is 'about'

48. NP-P-WH-TO-INF
"They made a great fuss about whether to go."
Agent V Oblique Prep() Topic[+wheth_inf]
*In this case, Prep() is 'about'

49. NP-PP
"She added the flowers to the bouquet."
Agent V Theme Prep() Destination
*In this case, Prep() is 'to'

50. NP-PP-PRED
"I considered that problem of little concern."
Experiencer V Oblique Prep(of) Predicate[-sentential]


51. NP-PRED-RS
"He seems a fool."
Theme V Predicate[-sentential]

52. NP-S
"He told the audience that he was leaving."
Actor V Recipient Topic[+that_comp]


53. NP-TO-INF-OC
"I advised Mary to go."
Actor V Recipient Predicate[+oc_to_inf]

54. NP-TO-INF-SC
"John promised Mary to resign."
Agen V Recipient Predicate[+sc_to_inf]

55. NP-TO-INF-VC
"They badgered him to go."
Agent V Recipient Predicate[+vc_to_inf]

56. NP-TO-NP
"He gave a big kiss to his mother."
Agent V Theme Prep(to) Recipient

**57. NP-TOBE
"I found him to smoke."
Agent V Theme Predicate[+small_clause]

58. NP-VEN-NP-OMIT
"He wanted the children found."
Experiencer V Cause Predicate[+ppart]

59. NP-WH-S
"They asked him whether he was going."
Agent V Recipient Topic[+wh_comp]

60. NP-WHAT-S
"They asked him what he was doing."
Agent V Recipient Topic[+what_extract]

61. NP-WH-TO-INF
"He asked him whether to clean the house."
Agent V Recipient Topic[+wheth_inf]

62. NP-WHAT-TO-INF
"He asked him what to do."
Agent V Recipient Topic[+what_inf]

63. P-ING-SC
"They failed in attempting the climb."
Agent V Prep() Predicate[+sc_ing]
*In this case, Prep() is 'in'

64. P-ING-AC
"They diaspproved of attempting the climb."
Agent V Prep() Predicate[+ac_ing]

*65. P-NP-ING
"They worried about him drinking."
Experiencer V Prep() Theme Topic[+gerund -control_ing -np_omit_ing -poss_ing -acc_ing]
*In this case, Prep() is 'about'

66. P-NP-TO-INF(-SC)
"He conspired with them to do it."
Actor1 V Prep() Actor2 Predicate[+sc_to_inf]
*In this case, Prep() is 'with'

67. P-NP-TO-INF-OC
"He beckoned to them to come."
Agent V Prep() Recipient Predicate[+oc_to_inf]
*In this case, Prep() is 'to'

68. P-NP-TO-INF-VC
"She appealed to him to go."
Agent V Prep() Recipient Predicate[+vc_to_inf]

69. P-POSSING
"They argued about his coming."
Actor[+plural] V Prep[+poss_ing]

70. P-WH-S
"He thought about whether he wanted to go."
Experiencer V Prep() Topic[+wh_comp]

71. P-WHAT-S
"He thought about what he wanted."
Experiencer V Prep() Cause[+what_extract]
*In this case, Prep() is 'about'

72. P-WH-TO-INF
"He thought about whether to go."
Experiencer V Prep() Topic[+wheth_inf]

73. P-WHAT-TO-INF
"He thought about what to do."
Experiencer V Prep() Cause[+what_inf]
*In this case, Prep() is 'about'

74. PART
"She gave up"
Agent V PART
*In this case, PART is 'up'

75. PART-ING-SC
"He ruled out paying her debts."
Agent V PART Predicate[+sc_ing]
*In this case, PART is 'out'

76. PART-NP/NP-PART
"I looked up the entry."
"I looked the entry up."
Experiencer V Cause up
Experiencer V up Cause

77. PART-NP-PP
"I separated out the three boys from the crowd."
"I separated the three boys out from the crowd."
Experiencer V PART Theme Prep(from) Source
Experiencer V Theme PART Prep(from) Source
*In this case, PART is 'from'

78. PART-PP
"She looked in on her friend."
Agent V in Prep() Theme

79. PART-WH-S
"They figured out whether she hadn't done her job."
Experiencer V PART Topic[+wh_comp]
Experiencer V Topic[+wh_comp] PART

80. PART-WH-S
"They figured out what she hadn't done."
Experiencer V PART Topic[+extracted]
Experiencer V Topic[+extracted] PART

81. PART-WH-TO-INF
"They figured out whether to go."
Experiencer V PART Topic[+wheth_inf]
Experiencer V Topic[+wheth_inf] PART

82. PART-WHAT-TO-INF
"They figured out what to do."
Experiencer V PART Topic[+what_inf]
Experiencer V Topic[+what_inf] PART

83. PART-THAT-S
"They figured out that she hadn't done her job."
Experiencer V PART Topic[+that_comp]
Expereincer V Topic[+that_comp] PART

84. POSSING
"He dismissed their writing novels."
Agent V Topic[+poss_ing]

85. POSSING-PP
"She attributed his drinking too much to his anxiety."
Agent V Theme[+poss_ing] Prep(to) Destination

86. ING-PP
"They limited smoking a pipe to the lounge."
Agent V Predicate[+gerund] Prep(to) Location

87. PP
"They apologized to him."
Agent V Prep(to) Recipient

88. PP-FOR-TO-INF
"They contracted with him for the man to go."
Actor1 V Prep(with) Actor2 for Theme Predicate[+to_inf]

89. PP-HOW-S
"He explained to her how she did it."
Agent V Prep(to) Recipient Topic[+how_extract]

90. PP-HOW-TO-INF
"He explained to them how to do it."
Agent V Prep(to) Recipient Topic[+wh_inf]

91. PP-P-WH-S
"I agreed with him about whether he should kill the peasants."
Actor1 V Prep(with) Actor2 Prep() Topic[+wh_comp]
*In this case, Prep() is 'about'

92. PP-P-WHAT-S
"I agreed with him about what he should do."
Actor1 V Prep(with) Actor2 Prep() Topic[+what_extract]
*In this case, Prep() is 'about'

93. PP-P-WHAT-TO-INF
"I agreed with him about what to do."
Actor1 V Prep(with) Actor2 Prep() Topic[+what_inf]

94. PP-P-WH-TO-INF
"I agreed with him about whether to go."
Actor1 V Prep(with) Actor2 Prep() Topic[+wheth_inf]
*In this case, Prep() is 'about'

95. PP-PP
"They flew from London to Rome."
Theme V Prep(from) Source Prep(to) Destination

96. PP-PRED-RS
"The matter seems in dispute."
Theme V Prep(in) Oblique[-sentential]

97. PP-THAT-S
"They admitted to the authorities that had entered illegally."
Agent V Prep(to) Recipient Topic[+that_comp]

98. PP-THAT-S-SUBJUNCT
"They suggested to him that he go."
Agent V Prep(to) Recipient Topic[+that_comp -tensed_that]

99. PP-TO-INF-RS
"He appeared to her to be ill."
Theme V Prep() Theme to be Predicate[-sentential]

100. PP-WH-S
"They asked about everybody they had enrolled."
Agent V Prep() Theme

101. PP-WHAT-S
"They asked about John what he had done."
Actor V Prep() Recipient Topic[+what_extract]

102. PP-WH-TO-INF
"They deduced from Kim whether to go."
Agent V Prep() Source Topic[+wheth_inf]

103. PP-WHAT-TO-INF
"They deduced from Kim what to do."
Agent V Prep() Source Topic[+what_inf]

104. S
"They thought that he was always late."
Experiencer V Topic[+that_comp]

105. S-SUBJ-S-OBJ
"For him to report the theft indicates that he wasn't guilty."
Cause[+indicative] V Oblique[+indicative]

106. S-SUBJUNCT
"She demanded that he leave immediately."
Agent V Topic[+that_comp -tensed_that]

107. SEEM-S
"It seems that they left."
it V(seem) Topic[+that_comp]

108. SEEM-TO-NP-S
"It seems to her that they were wrong."
it V Prep() Experiencer Content[+that_comp]

109. THAT-S
"He complained that they were coming."
Actor V Topic[+that_comp]

110. TO-INF-AC
"He helped to save the child."
Agent V Predicate[+ac_to_inf]

111. TO-INF-RS
"He seemed to come."
Theme V Predicate[+rs_to_inf]

112. TO-INF-SC
"I wanted to come."
Experiencer V Cause[+sc_to_inf]

113. WH-S
"He asked whether he should come."
Agent V Topic[+wh_comp]

114. WHAT-S
"He asked what he should do."
Agent V Topic[+what_extract]

115. WH-TO-INF
"He asked whether to clean the house."
Agent V Topic[+wheth_inf]

116. WHAT-TO_INF
"He asked what to do."
Agent V Topic[+what_inf]


***************************************
#s 117-163 do not appear in COMLEX
-SCFs with OK next to the name have been
checked to ensure that the title does
not already occur in the COMLEX lexicon
***************************************
117-163

#117. NP-PART-NP
"I opened him up a  new bank account."
Agent V Theme PART Predicate[-sentential]
*In this case, PART is 'up'

#118. XTAG:Light-verbs (various classes)

#119. PART-PP
"He breaks away from the abbey."
Agent V PART Prep(from) Source

120. NP-PART-PP
"He brought a book back for me."
Agent V Theme PART Prep(for) Recipient
Agent V PART Theme Prep(for) Recipient

121. PART-PP-PP
"He came down on him for his bad behaviour."
Agent V PART Prep(on) Location Prep(for) Cause[-sentential]

122. NP-PP-PP
"He turned it from a disaster into a victory."
Agent V Patient Prep(from) Source Prep(into) Destination

123. NP
"It cost ten pounds."
Agent V Asset

124. NP-NP
"It cost him ten pounds."
Agent V Theme Asset

#125.
"It set him back ten pounds."

#126. PART-ADVP
"He came off badly."
Agent V PART Advb

*127. ADVP-PP
"Things augur well for him."
Theme V Advb Prep(for) Patient

#128. EXTRAP-S
"It turns out that he did it."
it V Topic[+sentential]

129. S-SUBJ
"That he came matters."
Theme[+that_comp] V

#130.
"He had her on that he attended."

#131. PART-PP-S
"She gets through to him that he came."
Agent V PART Prep(to) Recipient Topic[+sentential]

#132. NP-NP-S
"He bet her ten pounds that he came."
Agent V Recipient Asset Topic[+sentential]

#133. NP-S
"He petitioned them that he be freed."
Agent V Recipient Topic[+sentential]
(How diff from 52?)

#134.
"I would appreciate it if he came."

135.
"It dawned on him what he should do."
it V Prep() Theme Topic[+what_extract]

136.
"He turned out a fool."
Theme V PART Oblique

#137.
"He started out poor."


#138.
"He turned out to be a crook."

#139.
"He set out to win."

#140.
"He got around to leaving."


#141.
"He got given a book."

142.
"He dared dance."
Agent V Predicate[+bare_inf]


143.
"He strikes me as foolish."
Theme V Expereincer as Predicate[-sentential]

144.
"He considers Fido a fool."
Agent V Theme Predicate[-sentential]


145.
"He makes him out crazy."
Agent V Theme PART Predicate[-sentential]

#146.
"He sands it down smooth."


147.
"He condemned him as stupid."
Agent V Theme as Predicate[-sentential]


#148.
"He put him down as stupid."


149.
"He made him out to be crazy."
Agent V Theme PART to be Predicate[-sentential]

#150.
"He spurred him on to try."


#151.
"He kept on at him to join."

#152.
"He talked him around into leaving."

#153.
"He looked at him leave."

*154.
"to see them hurts."

#155.
"He stood it alone."

156.
"He asked him how he came."
Agent V Recipient Topic[+how_extract]

#157.
"He gave money for him to go."

#158.
"It is believed that he came."

159.
"He seems as is he is clever."

160.
"It carves easily."

#161.
"He felt a fool."
Agent V Predicate[-sentential]

#162.
"He accepted him as associated."

#163.
"He accepted him as being normal."

