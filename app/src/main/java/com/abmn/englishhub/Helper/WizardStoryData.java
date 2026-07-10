package com.abmn.englishhub.Helper;

import com.abmn.englishhub.Model.WizardStoryModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Hardcoded content for the "ইতিহাসের অদ্ভুত পাতা" (Wizard) chapter — ten
// true, verifiable strange-history stories with English text, a Bangla
// translation, and grammar/translation notes. This is static editorial
// content, not user data, so it lives in code rather than behind an API.
public class WizardStoryData {

    public static List<WizardStoryModel> getAll() {
        List<WizardStoryModel> list = new ArrayList<>();

        list.add(new WizardStoryModel(
                1,
                "The Wave That Was Sweeter Than Water",
                "Boston, 1919",
                Arrays.asList(
                        "On a warm January afternoon in Boston, in 1919, something strange happened that nobody could have predicted: a wave of molasses destroyed part of a city.",
                        "A giant steel tank near the harbor held over two million gallons of molasses, waiting to be turned into rum. The tank had never been properly tested, and cracks had been forming for years. That afternoon, without warning, it burst open with a sound like a train crash.",
                        "A dark, sticky wave — twenty-five feet high — rushed through the streets at nearly 35 miles an hour, faster than a person could run. It swallowed buildings, knocked over a railway, and swept away everything in its path. Twenty-one people lost their lives, and it took weeks of hard work, using salt water from the harbor, just to wash the streets clean.",
                        "Even today, people in Boston say that on a hot summer day, if you stand near the old harbor, you can still catch a faint, sweet smell in the air."
                ),
                "যে ঢেউ পানির চেয়েও মিষ্টি ছিল",
                Arrays.asList(
                        "১৯১৯ সালের এক উষ্ণ জানুয়ারির বিকেলে, বোস্টন শহরে এমন একটা ঘটনা ঘটেছিল যা কেউ কল্পনাও করতে পারেনি — গুড়ের একটা ঢেউ শহরের একটা অংশ ধ্বংস করে দিয়েছিল।",
                        "বন্দরের কাছে একটা বিশাল ইস্পাতের ট্যাংকে প্রায় বিশ লক্ষ গ্যালনেরও বেশি গুড় জমা ছিল, যেটা পরে রাম বানানোর কাজে লাগার কথা ছিল। ট্যাংকটা কখনো ঠিকভাবে পরীক্ষা করা হয়নি, আর বছরের পর বছর ধরে এর গায়ে ফাটল ধরছিল। সেই বিকেলে, কোনো পূর্বাভাস ছাড়াই, ট্রেন দুর্ঘটনার মতো বিকট শব্দ করে ট্যাংকটা ফেটে যায়।",
                        "গাঢ়, আঠালো একটা ঢেউ — পঁচিশ ফুট উঁচু — প্রায় ৩৫ মাইল বেগে রাস্তা দিয়ে ছুটে আসে, যা একজন মানুষের দৌড়ানোর চেয়েও দ্রুত। এটা বাড়িঘর গ্রাস করে নেয়, একটা রেললাইন উপড়ে ফেলে, আর পথে যা কিছু ছিল সব ভাসিয়ে নিয়ে যায়। একুশজন মানুষ প্রাণ হারায়, আর রাস্তাগুলো পরিষ্কার করতে বন্দরের নোনা পানি ব্যবহার করে সপ্তাহের পর সপ্তাহ কঠোর পরিশ্রম করতে হয়েছিল।",
                        "আজও বোস্টনের মানুষজন বলে, গরমের কোনো দিনে পুরনো বন্দরের কাছে দাঁড়ালে নাকি বাতাসে এখনো একটা হালকা মিষ্টি গন্ধ পাওয়া যায়।"
                ),
                Arrays.asList(new String[][]{
                        {"গ্রামার নোট", "\"faster than a person could run\" — এখানে \"faster than X could Y\" প্যাটার্নে তুলনা করা হয়েছে। বাংলায় ক্রিয়াকে (run) আগে বিশেষ্যে পরিণত করে (দৌড়ানো), তারপর \"চেয়েও দ্রুত\" বসিয়ে তুলনাটা করা হয়।"}
                })
        ));

        list.add(new WizardStoryModel(
                2,
                "The Soldier Who Fought a War That Had Already Ended",
                "Lubang Island, 1944–1974",
                Arrays.asList(
                        "In 1944, a young Japanese intelligence officer named Hiroo Onoda was sent to a small island in the Philippines called Lubang, with clear orders: never surrender, and never stop fighting, no matter what happened.",
                        "World War II ended in 1945. But Onoda did not believe it. When leaflets fell from the sky announcing Japan's surrender, he was told they were enemy tricks. When newspapers were left for him to find, he thought they were fake, made to fool him into giving up. So he stayed in the jungle, living off coconuts and stolen rice, hiding from search parties for years.",
                        "Ten years passed. Then twenty. Onoda kept fighting a war that had already ended, loyal to an order nobody had cancelled.",
                        "Finally, in 1974 — twenty-nine years later — his own former commanding officer flew all the way to the Philippines and personally told him, face to face, that the war was over. Only then did Onoda lay down his sword."
                ),
                "যে সৈনিক এমন এক যুদ্ধ লড়েছিল যা আগেই শেষ হয়ে গিয়েছিল",
                Arrays.asList(
                        "১৯৪৪ সালে, হিরোও ওনোদা নামের একজন তরুণ জাপানি গোয়েন্দা অফিসারকে ফিলিপাইনের লুবাং নামের একটা ছোট্ট দ্বীপে পাঠানো হয়েছিল, একদম স্পষ্ট নির্দেশ দিয়ে — যাই ঘটুক না কেন, কখনো আত্মসমর্পণ করবে না, লড়াই থামাবে না।",
                        "দ্বিতীয় বিশ্বযুদ্ধ শেষ হয় ১৯৪৫ সালে। কিন্তু ওনোদা এটা বিশ্বাস করেনি। যখন আকাশ থেকে জাপানের আত্মসমর্পণের কথা জানিয়ে লিফলেট পড়ল, তাকে বলা হয়েছিল এগুলো শত্রুপক্ষের চাল। যখন তার জন্য পত্রিকা রেখে দেওয়া হলো যেন সে সেটা খুঁজে পায়, সে ভাবল এগুলো নকল, তাকে ভুলিয়ে হার মানাতে বানানো। তাই সে জঙ্গলেই থেকে গেল, নারকেল আর চুরি করা চাল খেয়ে বছরের পর বছর খোঁজ-পার্টিগুলো থেকে লুকিয়ে বেঁচে রইল।",
                        "দশ বছর পেরিয়ে গেল। তারপর বিশ বছর। ওনোদা তখনও এমন একটা যুদ্ধ লড়ে যাচ্ছিল যা আগেই শেষ হয়ে গিয়েছে, এমন একটা আদেশের প্রতি অনুগত থেকে যা কেউ কখনো বাতিল করেনি।",
                        "শেষ পর্যন্ত, ১৯৭৪ সালে — ঊনত্রিশ বছর পর — তার নিজের সাবেক কমান্ডিং অফিসার সুদূর ফিলিপাইনে উড়ে গিয়ে সামনাসামনি তাকে জানালেন যে যুদ্ধ শেষ হয়ে গেছে। তবেই ওনোদা তার তলোয়ার নামিয়ে রাখল।"
                ),
                Arrays.asList(new String[][]{
                        {"গ্রামার নোট", "\"an order nobody had cancelled\" — ইংরেজিতে বিশেষ্যের (order) বর্ণনাটা তার পরে বসে (relative clause after the noun)। বাংলায় আমরা প্রায়ই এই বর্ণনাটা বিশেষ্যের আগে এনে বসাই: \"এমন একটা আদেশের প্রতি... যা কেউ কখনো বাতিল করেনি\"।"}
                })
        ));

        list.add(new WizardStoryModel(
                3,
                "The War Australia Lost to Birds",
                "Western Australia, 1932",
                Arrays.asList(
                        "In 1932, farmers in Western Australia were facing a strange enemy: nearly twenty thousand wild emus, huge flightless birds, trampling their wheat fields.",
                        "The government decided this called for a military solution. Soldiers were sent out with two machine guns, under the command of a major, with a simple goal: shoot the emus and end the problem in a few days.",
                        "It did not go as planned. The emus scattered the moment the guns fired, running in every direction at speeds a soldier could barely match. The machine guns kept jamming in the dust. After weeks of trying, only a few hundred emus had been killed, out of thousands of bullets fired.",
                        "Newspapers began mocking the army for losing to birds, and the government quietly called off the operation. Today, it is remembered, half-jokingly, as \"The Great Emu War\" — a war Australia never actually won."
                ),
                "যে যুদ্ধ অস্ট্রেলিয়া পাখির কাছে হেরে গিয়েছিল",
                Arrays.asList(
                        "১৯৩২ সালে, পশ্চিম অস্ট্রেলিয়ার কৃষকরা এক অদ্ভুত শত্রুর মুখোমুখি হয়েছিলেন — প্রায় বিশ হাজার বুনো এমু, বিশাল আকারের উড়তে-না-পারা পাখি, যারা তাদের গমের ক্ষেত পায়ে মাড়িয়ে নষ্ট করে দিচ্ছিল।",
                        "সরকার ঠিক করল এর জন্য একটা সামরিক সমাধান দরকার। একজন মেজরের নেতৃত্বে দুটো মেশিনগান নিয়ে সৈন্য পাঠানো হলো, একটাই সহজ লক্ষ্য নিয়ে — এমুগুলোকে গুলি করে কয়েক দিনেই সমস্যা শেষ করে দেওয়া।",
                        "কিন্তু পরিকল্পনামতো কিছুই হলো না। বন্দুক গর্জে ওঠার সাথে সাথেই এমুরা চারদিকে ছড়িয়ে পড়ল, এমন গতিতে দৌড়াতে লাগল যা একজন সৈন্যের পক্ষে ধরাই মুশকিল হয়ে গেল। কয়েক সপ্তাহ চেষ্টার পরও, হাজার হাজার গুলি ছোড়া হলেও মাত্র কয়েকশ এমু মারা পড়েছিল।",
                        "পত্রিকাগুলো সেনাবাহিনীকে পাখির কাছে হেরে যাওয়ার জন্য ঠাট্টা করতে শুরু করল, আর সরকার চুপচাপ পুরো অভিযান বন্ধ করে দিল। আজও এটাকে আধা-মজার ছলে মনে করা হয় \"The Great Emu War\" নামে।"
                ),
                Arrays.asList(new String[][]{
                        {"গ্রামার নোট ১", "\"at speeds a soldier could barely match\" — এখানে \"that/which\" ছাড়াই একটা relative clause বসেছে (zero relative pronoun), যা ইংরেজিতে খুব স্বাভাবিক। বাংলায় অনুবাদের সময় সেই সম্পর্কটা স্পষ্ট করে আনতে হয়: \"যা একজন সৈন্যের পক্ষে ধরাই মুশকিল\"।"},
                        {"গ্রামার নোট ২", "\"It did not go as planned\" — এটা একটা কমন এক্সপ্রেশন, মানে \"যেমন ভাবা হয়েছিল সেভাবে হয়নি\"। এই ধরনের ছোট everyday এক্সপ্রেশন মুখস্থ রাখলে গল্প/আর্টিকেল পড়া সহজ হয়ে যায়।"}
                })
        ));

        list.add(new WizardStoryModel(
                4,
                "The Laughter That Shut Down a Country's Schools",
                "Tanganyika, 1962",
                Arrays.asList(
                        "In January 1962, at a girls' boarding school in Tanganyika (now Tanzania), three students suddenly burst into laughter — and could not stop.",
                        "The laughter did not fade after a few minutes, or even a few hours. It lasted for days, and it spread. More students began laughing uncontrollably, some crying from exhaustion, unable to explain why. The school had no choice but to shut its doors.",
                        "But closing the school did not end it. Students carried it home to their villages, and from there, it reached other schools nearby. Over the following months, well over a thousand people were affected across the region.",
                        "Doctors who studied the outbreak could find no virus, no poison, nothing physical to explain it. They concluded it was a case of mass hysteria — stress spreading through a community like an emotional chain reaction, wearing the strange, harmless mask of laughter."
                ),
                "যে হাসি একটা দেশের স্কুল বন্ধ করে দিয়েছিল",
                Arrays.asList(
                        "১৯৬২ সালের জানুয়ারিতে, তানজানিয়ার (তখনকার টাঙ্গানিয়িকা) একটা মেয়েদের বোর্ডিং স্কুলে, হঠাৎ তিনজন ছাত্রী হাসতে শুরু করল — আর থামতেই পারল না।",
                        "কয়েক মিনিট বা কয়েক ঘণ্টায়ও এই হাসি থামল না। এটা কয়েকদিন ধরে চলল, আর ছড়িয়েও পড়ল। আরও অনেক ছাত্রী অনিয়ন্ত্রিতভাবে হাসতে শুরু করল, কেউ কেউ ক্লান্তিতে কাঁদতেও শুরু করল। স্কুল কর্তৃপক্ষের সামনে দরজা বন্ধ করে দেওয়া ছাড়া আর কোনো উপায় থাকল না।",
                        "কিন্তু স্কুল বন্ধ করেও এটা থামল না। ছাত্রীরা এটা নিজেদের গ্রামে বয়ে নিয়ে গেল, আর সেখান থেকে আশেপাশের অন্য স্কুলেও ছড়িয়ে পড়ল। পরের কয়েক মাসে, পুরো এলাকা জুড়ে হাজারেরও বেশি মানুষ এতে আক্রান্ত হয়েছিল।",
                        "যেসব ডাক্তার এই ঘটনা নিয়ে গবেষণা করেছিলেন, তারা কোনো ভাইরাস, কোনো বিষ, কোনো শারীরিক কারণ খুঁজে পাননি। তারা সিদ্ধান্তে এলেন এটা এক ধরনের \"mass hysteria\" — মানসিক চাপ একটা সমাজে চেইন-রিঅ্যাকশনের মতো ছড়িয়ে পড়েছিল, শুধু হাসির অদ্ভুত, নিরীহ ছদ্মবেশে।"
                ),
                Arrays.asList(new String[][]{
                        {"গ্রামার নোট ১", "\"could not stop\" — \"could\" দিয়ে অতীতকালে ability বোঝানো হয়। লক্ষ করো ইংরেজিতে \"not\" বসে \"could\"-এর পরে, বাংলায় \"পারল না\"-তে \"না\" শেষে বসে — এই word-order পার্থক্যটা মনে রাখা দরকার।"},
                        {"গ্রামার নোট ২", "\"had no choice but to shut its doors\" — এই \"no choice but to + verb\" প্যাটার্নটা একটা কমন ইডিয়ম (\"এছাড়া আর উপায় ছিল না\")। আক্ষরিক অনুবাদ না করে পুরো প্যাটার্নটাকে স্বাভাবিক বাংলা এক্সপ্রেশন দিয়ে বদলাতে হয়।"}
                })
        ));

        list.add(new WizardStoryModel(
                5,
                "The Smell That Forced a Government to Act",
                "London, 1858",
                Arrays.asList(
                        "In the summer of 1858, London faced a crisis that no army or disease could be blamed for — a smell.",
                        "For years, the River Thames had been used as an open sewer. That summer was unusually hot, and the heat turned the river into something unbearable. The stench rose so strongly that it reached all the way into the Houses of Parliament, sitting right on the riverbank.",
                        "Members of Parliament reportedly could not concentrate. Curtains were soaked in chemicals and hung over the windows, just to make the smell bearable enough to keep working. Newspapers called it \"The Great Stink.\"",
                        "Within weeks, Parliament approved funding for a massive new sewer system, designed by engineer Joseph Bazalgette. It transformed London's health and helped end deadly cholera outbreaks. In the end, it was not medicine or protest that fixed London's sewage crisis — it was the smell simply becoming impossible to ignore."
                ),
                "যে দুর্গন্ধ একটা সরকারকে কাজ করতে বাধ্য করেছিল",
                Arrays.asList(
                        "১৮৫৮ সালের গ্রীষ্মে, লন্ডন এমন একটা সংকটের মুখোমুখি হয়েছিল যার জন্য কোনো সেনাবাহিনী বা রোগকে দোষ দেওয়া যায় না — একটা দুর্গন্ধ।",
                        "বছরের পর বছর ধরে, টেমস নদীকে খোলা ড্রেন হিসেবে ব্যবহার করা হচ্ছিল। সেই গ্রীষ্মটা ছিল অস্বাভাবিক গরম, আর সেই গরম নদীটাকে অসহনীয় কিছুতে পরিণত করে দিল। এমনভাবে দুর্গন্ধ উঠতে লাগল যে সেটা নদীর তীরে বসা পার্লামেন্ট ভবন পর্যন্ত পৌঁছে গেল।",
                        "শোনা যায়, পার্লামেন্ট সদস্যরা মনোযোগই দিতে পারছিলেন না। জানালার পর্দাগুলো রাসায়নিকে ভিজিয়ে ঝুলিয়ে দেওয়া হলো, কাজ চালিয়ে যাওয়ার মতো সহনীয় করে তুলতে। পত্রিকাগুলো একে বলল \"The Great Stink\"।",
                        "কয়েক সপ্তাহের মধ্যেই, পার্লামেন্ট জোসেফ বাজালজেট নামের প্রকৌশলীর ডিজাইন করা একটা বিশাল নতুন sewer সিস্টেমের জন্য অর্থ অনুমোদন করল। শেষ পর্যন্ত, লন্ডনের সংকট সমাধান করেছিল কোনো ওষুধ বা প্রতিবাদ না — সমাধান করেছিল সেই দুর্গন্ধ, যেটাকে আর উপেক্ষা করার উপায়ই ছিল না।"
                ),
                Arrays.asList(new String[][]{
                        {"গ্রামার নোট ১", "\"a crisis that no army or disease could be blamed for\" — এখানে preposition (\"for\") বাক্যের শেষে বসেছে, যা ইংরেজির খুব কমন স্টাইল। বাংলায় \"for\"-এর অর্থ আগেই এনে ফেলতে হয়: \"যার জন্য... দোষ দেওয়া যায় না\"।"},
                        {"গ্রামার নোট ২", "\"it was not medicine... it was the smell\" — এটা একটা cleft sentence, জোর দেওয়ার জন্য \"it was X that...\" গঠন। বাংলায় জোর দিতে আমরা প্রায়ই একই শব্দ বাক্যের শেষে টেনে আনি।"}
                })
        ));

        list.add(new WizardStoryModel(
                6,
                "The Day an Entire Country Switched Sides of the Road",
                "Sweden, 1967",
                Arrays.asList(
                        "At exactly 5 o'clock in the morning on September 3rd, 1967, every car in Sweden came to a stop.",
                        "For as long as anyone could remember, Swedish drivers had driven on the left side of the road — but almost every country around them drove on the right. The government decided it was time to change, all at once, across the entire country.",
                        "The day was called \"Dagen H\" — H Day. All non-essential traffic was banned for hours before and after the changeover. When the moment came, drivers carefully crossed to the other side of the road and continued on their way.",
                        "Years of planning had gone into that single morning. Remarkably, the switch caused far fewer accidents than expected, mostly because every driver, without exception, was paying unusually close attention that day."
                ),
                "যে দিন একটা গোটা দেশ রাস্তার পাশ বদলে ফেলেছিল",
                Arrays.asList(
                        "১৯৬৭ সালের ৩রা সেপ্টেম্বর, ঠিক ভোর ৫টায়, সুইডেনের প্রতিটা গাড়ি থেমে গেল।",
                        "যত দূর মনে করা যায়, সুইডিশ চালকরা রাস্তার বাম পাশ দিয়ে গাড়ি চালাতেন — কিন্তু তাদের চারপাশের প্রায় সব দেশই ডান পাশ দিয়ে চালাত। সরকার ঠিক করল এবার বদলানোর সময় এসেছে, একসাথে, পুরো দেশ জুড়ে।",
                        "দিনটার নাম দেওয়া হলো \"Dagen H\"। বদলের কয়েক ঘণ্টা আগে-পরে অপ্রয়োজনীয় সব যান চলাচল নিষিদ্ধ করা হলো। মুহূর্তটা এলে, চালকরা সাবধানে রাস্তার অন্য পাশে চলে গেলেন আর যাত্রা চালিয়ে গেলেন।",
                        "সেই একটামাত্র সকালের পেছনে বছরের পর বছর পরিকল্পনা ছিল। আশ্চর্যজনকভাবে, এই পরিবর্তনে প্রত্যাশার চেয়ে অনেক কম দুর্ঘটনা ঘটেছিল, মূলত কারণ সেদিন প্রতিটা চালকই অস্বাভাবিক রকম মনোযোগী ছিলেন।"
                ),
                Arrays.asList(new String[][]{
                        {"গ্রামার নোট ১", "\"For as long as anyone could remember\" — সময়ের ব্যাপ্তি বোঝানোর একটা ফিক্সড এক্সপ্রেশন, মানে \"যুগ যুগ ধরে\"।"},
                        {"গ্রামার নোট ২", "\"Years of planning had gone into...\" — past perfect বোঝায় পরিকল্পনাটা সেই সকালের আগেই সম্পন্ন হয়েছিল। বাংলায় সাধারণত এটা সরল past দিয়েই অনুবাদ হয়, কারণ সময়ের এই সূক্ষ্ম পার্থক্য প্রসঙ্গ থেকেই বোঝা যায়।"}
                })
        ));

        list.add(new WizardStoryModel(
                7,
                "The Town That Has Been on Fire Since 1962",
                "Pennsylvania, since 1962",
                Arrays.asList(
                        "Somewhere beneath the small town of Centralia, Pennsylvania, a fire has been burning, without stopping, for more than sixty years.",
                        "It began in 1962, most likely when a fire lit to burn trash spread into an old coal mine running underneath the town. Coal burns slowly and steadily, and once it catches fire underground, it is extremely difficult to put out.",
                        "Over time, the ground grew dangerously unstable. Sinkholes opened up without warning. Poisonous gas seeped up through cracks in the earth. In 1980, a boy nearly fell into a sudden sinkhole in his own backyard, filled with smoke and heat.",
                        "By the 1990s, the government had bought out and relocated almost everyone who lived there. Centralia, once home to over a thousand people, now has only a handful of residents left. The fire below is expected to keep burning for the next 250 years."
                ),
                "যে শহর ১৯৬২ সাল থেকে আগুনে জ্বলছে",
                Arrays.asList(
                        "আমেরিকার পেনসিলভানিয়ার ছোট্ট শহর Centralia-র মাটির নিচে কোথাও, একটা আগুন গত ষাট বছরেরও বেশি সময় ধরে, একটুও না থেমে, জ্বলছে।",
                        "এর শুরু ১৯৬২ সালে, সম্ভবত আবর্জনা পোড়ানোর একটা আগুন থেকে, যেটা শহরটার নিচের একটা পুরনো কয়লাখনিতে ছড়িয়ে পড়েছিল। কয়লা ধীরে ধীরে জ্বলে, আর একবার মাটির নিচে আগুন ধরে গেলে সেটা নেভানো অত্যন্ত কঠিন।",
                        "সময়ের সাথে সাথে, মাটি বিপজ্জনকভাবে অস্থির হয়ে উঠল। কোনো পূর্বাভাস ছাড়াই সিঙ্কহোল তৈরি হতে লাগল। ১৯৮০ সালে, একটা ছেলে তার নিজের বাড়ির উঠানেই হঠাৎ তৈরি হওয়া একটা সিঙ্কহোলে প্রায় পড়ে যাচ্ছিল।",
                        "১৯৯০-এর দশকের মধ্যে, সরকার প্রায় সবার বাড়িঘর কিনে নিয়ে সরিয়ে দিয়েছিল। এক সময় হাজারেরও বেশি মানুষের বাসস্থান Centralia-তে, এখন হাতেগোনা কয়েকজন বাকি আছে। নিচের আগুনটা আরও প্রায় ২৫০ বছর জ্বলতে থাকবে বলে ধারণা করা হয়।"
                ),
                Arrays.asList(new String[][]{
                        {"গ্রামার নোট ১", "\"has been burning... for more than sixty years\" — present perfect continuous, অতীতে শুরু হয়ে এখনও চলতে থাকা কাজ বোঝায়। বাংলায় এই ধারণা \"ধরে... জ্বলছে\" দিয়ে বোঝানো হয়।"},
                        {"গ্রামার নোট ২", "\"is expected to keep burning\" — \"be expected to + verb\" মানে \"ধারণা করা হয় যে... হবে\", ভবিষ্যতের একটা অনুমান বোঝাতে ব্যবহার হয়।"}
                })
        ));

        list.add(new WizardStoryModel(
                8,
                "The Cat the CIA Turned Into a Spy",
                "CIA, 1960s",
                Arrays.asList(
                        "During the Cold War, the CIA had a strange idea: what if a cat could become a spy?",
                        "In the 1960s, agency scientists spent years and millions of dollars on a secret project. They surgically placed a tiny microphone inside a cat's ear canal, a radio transmitter at the base of its skull, and a thin wire antenna woven into its fur.",
                        "After years of training, the cat was finally ready for its very first real mission. It was driven to a park near the target location and released from the van. Within seconds, before the cat had even reached its destination, it walked into the street and was struck and killed by a passing taxi.",
                        "The CIA officially shut the project down soon after. Decades later, once the documents were declassified, the world learned that one of history's most expensive spy attempts had ended, quite literally, before it had even begun."
                ),
                "যে বিড়ালকে সিআইএ গুপ্তচর বানিয়েছিল",
                Arrays.asList(
                        "স্নায়ুযুদ্ধের সময়, সিআইএ-র মাথায় একটা অদ্ভুত আইডিয়া এসেছিল: যদি একটা বিড়ালকেই গুপ্তচর বানানো যায়?",
                        "১৯৬০-এর দশকে, সংস্থার বিজ্ঞানীরা একটা গোপন প্রজেক্টে বছরের পর বছর আর লক্ষ লক্ষ ডলার খরচ করেছিলেন। তারা অস্ত্রোপচার করে বিড়ালের কানের ভেতরে একটা ক্ষুদ্র মাইক্রোফোন, খুলির গোড়ায় একটা রেডিও ট্রান্সমিটার, আর লোমের ভেতরে বোনা একটা তারের অ্যান্টেনা বসিয়ে দিয়েছিলেন।",
                        "বছরের পর বছর ট্রেনিং-এর পর, বিড়ালটা তার প্রথম আসল মিশনের জন্য প্রস্তুত হলো। ভ্যানে করে লক্ষ্যস্থলের কাছের একটা পার্কে নিয়ে গিয়ে ছেড়ে দেওয়া হলো। কয়েক সেকেন্ডের মধ্যেই, গন্তব্যে পৌঁছানোরও আগে, বিড়ালটা রাস্তায় নেমে গেল আর একটা চলন্ত ট্যাক্সির ধাক্কায় মারা গেল।",
                        "এর কিছুদিন পরই সিআইএ প্রজেক্টটা বন্ধ করে দেয়। কয়েক দশক পর, নথিগুলো প্রকাশ্যে আসার পর, বিশ্ব জানতে পারল যে ইতিহাসের সবচেয়ে ব্যয়বহুল গুপ্তচরবৃত্তির একটা প্রচেষ্টা শেষ হয়ে গিয়েছিল শুরু হওয়ারও আগে।"
                ),
                Arrays.asList(new String[][]{
                        {"গ্রামার নোট ১", "\"what if a cat could become a spy?\" — \"What if...?\" কাল্পনিক সম্ভাবনা প্রশ্ন আকারে তুলে ধরার প্যাটার্ন, বাংলায় \"যদি... তাহলে কী হয়?\"।"},
                        {"গ্রামার নোট ২", "\"before the cat had even reached its destination\" — past perfect (\"had... reached\") বোঝায় এই ঘটনাটা আরেকটা past ঘটনার আগেই ঘটার কথা ছিল কিন্তু ঘটেইনি। \"even\" জোর দিচ্ছে বিস্ময় বোঝাতে।"}
                })
        ));

        list.add(new WizardStoryModel(
                9,
                "The Summer That Never Came",
                "Global, 1816",
                Arrays.asList(
                        "In 1816, summer simply did not arrive for much of Europe and North America.",
                        "The cause had begun a year earlier, when a volcano named Tambora, in modern-day Indonesia, erupted with more force than any volcano in recorded history. Ash and gas were thrown so high into the sky that they spread across the entire planet, quietly blocking out sunlight for months.",
                        "By the following summer, snow fell in June. Frost killed crops in July and August. Food became scarce, prices rose sharply, and in some regions, people starved.",
                        "That same cold, dark summer, a young writer named Mary Shelley was staying by a lake in Switzerland with friends, trapped indoors by the strange weather. They began telling each other ghost stories to pass the time — and from that evening came the idea for her novel \"Frankenstein,\" born, quite literally, out of a summer that never came."
                ),
                "যে গ্রীষ্মকাল কখনো আসেনি",
                Arrays.asList(
                        "১৮১৬ সালে, ইউরোপ আর উত্তর আমেরিকার বেশিরভাগ জায়গায় গ্রীষ্মকালই আসেনি।",
                        "এর কারণ শুরু হয়েছিল এক বছর আগে, যখন বর্তমান ইন্দোনেশিয়ার Tambora আগ্নেয়গিরি রেকর্ড করা ইতিহাসের সবচেয়ে শক্তিশালী অগ্ন্যুৎপাত ঘটিয়েছিল। ছাই আর গ্যাস আকাশে এত উঁচুতে ছুঁড়ে দেওয়া হয়েছিল যে সেগুলো সারা পৃথিবীতে ছড়িয়ে পড়ে, কয়েক মাসের জন্য সূর্যের আলো আটকে রেখেছিল।",
                        "পরের গ্রীষ্মে, জুন মাসে বরফ পড়ল। জুলাই-আগস্টে তুষারপাতে ফসল নষ্ট হলো। খাবারের অভাব দেখা দিল, দাম বাড়ল, আর কিছু অঞ্চলে মানুষ অনাহারে থাকল।",
                        "সেই একই ঠান্ডা, অন্ধকার গ্রীষ্মে, মেরি শেলি নামের এক তরুণী লেখিকা সুইজারল্যান্ডের একটা হ্রদের ধারে বন্ধুদের সাথে ছিলেন, অদ্ভুত আবহাওয়ায় ঘরের ভেতর আটকে। তারা সময় কাটাতে ভূতের গল্প বলতে শুরু করলেন — আর সেখান থেকেই জন্ম নিল তার উপন্যাস \"Frankenstein\", এমন একটা গ্রীষ্ম থেকে যা কখনো আসেইনি।"
                ),
                Arrays.asList(new String[][]{
                        {"গ্রামার নোট ১", "\"summer simply did not arrive\" — \"simply\" এখানে ক্রিয়াকে জোর দিচ্ছে, মানে \"একদমই আসেনি\"। এরকম intensifier verb-এর আগে বসিয়ে জোর বাড়ানো একটা কমন কৌশল।"},
                        {"গ্রামার নোট ২", "\"born... out of a summer that never came\" — \"born out of X\" একটা রূপক এক্সপ্রেশন, মানে \"X থেকে জন্ম নেওয়া\"। সাহিত্যে idea-র উৎস বোঝাতে প্রায়ই ব্যবহার হয়।"}
                })
        ));

        list.add(new WizardStoryModel(
                10,
                "The Dance That Would Not Stop",
                "Strasbourg, 1518",
                Arrays.asList(
                        "In July of 1518, in the city of Strasbourg, a woman named Frau Troffea stepped into the street and began to dance.",
                        "There was no music playing. No celebration was happening. She simply danced, for hours, then for days, seemingly unable to stop herself. Within a week, dozens of others had joined her, dancing in the streets with the same strange, exhausted determination.",
                        "City officials, confused and alarmed, tried an unusual cure: they believed the dancers needed to dance the sickness out of their systems. So they built a wooden stage, hired musicians, and even brought in professional dancers to keep the rhythm going.",
                        "It made things worse. By the end of the summer, as many as four hundred people were dancing uncontrollably, and several reportedly died from heart attacks, strokes, or sheer exhaustion. Eventually, the dancing faded away, leaving behind one of history's strangest medical mysteries."
                ),
                "যে নাচ থামতেই চাইছিল না",
                Arrays.asList(
                        "১৫১৮ সালের জুলাই মাসে, স্ট্রাসবুর্গ শহরে, ফ্রাউ ট্রোফেয়া নামের এক নারী রাস্তায় নেমে নাচতে শুরু করলেন।",
                        "কোনো গান বাজছিল না। কোনো উৎসবও চলছিল না। তিনি শুধুই নাচলেন, ঘণ্টার পর ঘণ্টা, তারপর দিনের পর দিন, যেন নিজেকে থামাতেই পারছিলেন না। এক সপ্তাহের মধ্যেই, আরও কয়েক ডজন মানুষ একই রকম অদ্ভুত, ক্লান্ত দৃঢ়তা নিয়ে তার সাথে রাস্তায় নাচতে যোগ দিলেন।",
                        "শহরের কর্মকর্তারা একটা অস্বাভাবিক প্রতিকার চেষ্টা করলেন — তারা বিশ্বাস করতেন নর্তকদের শরীর থেকে \"রোগটা\" নাচের মধ্য দিয়েই বের করে দিতে হবে। তাই তারা কাঠের মঞ্চ বানিয়ে বাদ্যকর ভাড়া করলেন, তাল ধরে রাখার জন্য পেশাদার নর্তকও আনলেন।",
                        "এতে অবস্থা আরও খারাপ হলো। গ্রীষ্মের শেষে, প্রায় চারশো মানুষ অনিয়ন্ত্রিতভাবে নাচছিলেন, আর কয়েকজন হার্ট অ্যাটাক, স্ট্রোক, বা ক্লান্তিতে মারা গিয়েছিলেন বলে জানা যায়। শেষ পর্যন্ত নাচটা মিলিয়ে গেল, রেখে গেল ইতিহাসের অন্যতম অদ্ভুত চিকিৎসা রহস্য।"
                ),
                Arrays.asList(new String[][]{
                        {"গ্রামার নোট ১", "\"seemingly unable to stop herself\" — \"seemingly\" মানে \"দেখে মনে হচ্ছিল যেন\", নিশ্চিত না হয়ে অনুমান প্রকাশ করার কৌশল। বাংলায় \"যেন\" দিয়ে বোঝানো হয়।"},
                        {"গ্রামার নোট ২", "\"It made things worse\" — \"make + object + adjective\" গঠন ফলাফল বোঝাতে খুব কমন। বাংলায় \"worse\"-কে ক্রিয়াবিশেষণ বানিয়ে \"অবস্থা আরও খারাপ হলো\" আকারে অনুবাদ হয়।"}
                })
        ));

        return list;
    }
}
